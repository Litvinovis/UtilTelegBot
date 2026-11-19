package com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import com.utilteleg.bot.model.Campaign;
import com.utilteleg.bot.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Добавляем импорты для Apache POI
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class TelegramBotService extends DefaultLongPollingUpdateConsumer implements SpringLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    
    private final String botUsername;
    private final String botToken;
    private final TelegramClient telegramClient;
    
    // В реальном приложении вы бы использовали правильную структуру данных или базу данных
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> userSelectedCampaign = new ConcurrentHashMap<>();
    private final Map<Long, String> userSelectedAgency = new ConcurrentHashMap<>();

    private final com.utilteleg.bot.config.AppConfig appConfig;
    private final StatisticsService statisticsService;

    public TelegramBotService(
            String botUsername,
            String botToken,
            com.utilteleg.bot.config.AppConfig appConfig,
            StatisticsService statisticsService,
            TelegramClient telegramClient) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.appConfig = appConfig;
        this.statisticsService = statisticsService;
        this.telegramClient = telegramClient;
    }
    
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration() {
        logger.info("Бот @{} зарегистрирован для long polling", botUsername);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            
            logger.info("Получено сообщение из чата {}: {}", chatId, messageText);
            
            if ("/start".equals(messageText)) {
                sendWelcomeMessage(chatId);
            } else if ("/campaigns".equals(messageText)) {
                sendCampaignsList(chatId);
            } else if (userStates.get(chatId) == null) {
                // Ответ по умолчанию
                sendWelcomeMessage(chatId);
            } else {
                handleUserResponse(chatId, messageText);
            }
        }
    }
    
    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Добро пожаловать в бот шаблонов заявлений в госорганы!\n" +
                "Используйте /campaigns для просмотра доступных тем.";
        
        SendMessage message = new SendMessage(chatId.toString(), welcomeText);
        
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить приветственное сообщение: {}", e.getMessage(), e);
        }
    }
    
    private void sendCampaignsList(Long chatId) {
        logger.info("Начало обработки запроса на получение списка кампаний");
        
        // Add detailed null check with logging
        if (appConfig == null) {
            logger.error("AppConfig бин равен null. Конфигурация не была внедрена правильно.");
            logger.error("Проверьте, что класс AppConfig помечен аннотациями @Configuration и @ConfigurationProperties(prefix = \"campaigns\")");
            logger.error("Также проверьте, что в основном классе Application есть аннотация @EnableConfigurationProperties");
            SendMessage errorMessage = new SendMessage(chatId.toString(), "Извините, произошла критическая ошибка при загрузке конфигурации. AppConfig недоступен.\n\n" +
                               "- AppConfig бин равен null\n" +
                               "- Конфигурация не была внедрена правильно\n" +
                               "- Проверьте аннотации класса AppConfig и Application");
            try {
                telegramClient.execute(errorMessage);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
            }
            return;
        }
        
        logger.debug("AppConfig бин доступен. Получение списка кампаний...");
        List<Campaign> campaigns = appConfig.getCampaigns();
        
        if (campaigns == null) {
            logger.error("Список кампаний равен null. Конфигурация не была загружена правильно.");
            logger.error("Диагностика:");
            logger.error("1. Проверьте, что файл application.yml существует в каталоге src/main/resources/");
            logger.error("2. Проверьте, что файл application.yml имеет правильный формат YAML");
            logger.error("3. Проверьте, что в файле application.yml есть секция 'campaigns:'");
            logger.error("4. Проверьте, что путь к файлу application.yml доступен приложению");
            logger.error("5. Проверьте, что классы Campaign и Agency имеют правильные геттеры/сеттеры");
            logger.error("6. Проверьте, что в application.yml нет синтаксических ошибок");
            
            // Дополнительная диагностика
            String configStatus = "Недоступно";
            try {
                configStatus = appConfig.getConfigurationStatus();
            } catch (Exception e) {
                logger.error("Ошибка при получении статуса конфигурации: {}", e.getMessage(), e);
            }
            
            SendMessage errorMessage = new SendMessage(chatId.toString(), "Извините, произошла ошибка при загрузке конфигурации. Список кампаний недоступен.\n\n" +
                               "Диагностика:\n" +
                               "- Список кампаний равен null\n" +
                               "- Статус конфигурации: " + configStatus + "\n" +
                               "- Проверьте файл application.yml\n" +
                               "- Проверьте правильность формата YAML\n" +
                               "- Убедитесь, что путь к файлу шаблона существует");
            try {
                telegramClient.execute(errorMessage);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
            }
            return;
        }
        
        if (campaigns.isEmpty()) {
            logger.warn("Список кампаний пуст. Проверьте файл конфигурации application.yml.");
            SendMessage warningMessage = new SendMessage(chatId.toString(), "Список доступных кампаний пуст. Пожалуйста, обратитесь к администратору бота.\n\n" +
                                 "Диагностика:\n" +
                                 "- Список кампаний пуст (0 элементов)\n" +
                                 "- Проверьте содержимое файла application.yml");
            try {
                telegramClient.execute(warningMessage);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить предупреждающее сообщение: {}", e.getMessage(), e);
            }
            return;
        }
        
        logger.info("Успешно получено {} кампаний из конфигурации", campaigns.size());
        String messageText = "Доступные кампании:\n\n" +
                IntStream.range(0, campaigns.size())
                        .mapToObj(i -> (i + 1) + ". " + campaigns.get(i).name())
                        .collect(Collectors.joining("\n")) +
                "\n\nПожалуйста, выберите кампанию, введя её номер.";
        
        SendMessage message = new SendMessage(chatId.toString(), messageText);

        userStates.put(chatId, "SELECTING_CAMPAIGN");
        
        try {
            telegramClient.execute(message);
            logger.info("Список кампаний успешно отправлен пользователю");
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить список кампаний: {}", e.getMessage(), e);
        }
    }
    
    private void handleUserResponse(Long chatId, String messageText) {
        switch (userStates.get(chatId)) {
            case "SELECTING_CAMPAIGN" -> handleCampaignSelection(chatId, messageText);
            case "SELECTING_AGENCY"  -> handleAgencySelection(chatId, messageText);
            case "SELECTING_DELIVERY"-> handleDeliverySelection(chatId, messageText);
            default                  -> sendWelcomeMessage(chatId);
        }
    }
    
    private void handleCampaignSelection(Long chatId, String messageText) {
        try {
            int campaignIndex = Integer.parseInt(messageText) - 1;
            List<Campaign> campaigns = appConfig.getCampaigns();
            
            if (campaignIndex >= 0 && campaignIndex < campaigns.size()) {
                Campaign selectedCampaign = campaigns.get(campaignIndex);
                userSelectedCampaign.put(chatId, selectedCampaign.id());

                List<Agency> agencies = selectedCampaign.agencies();
                String messageTextBuilder = "Доступные органы для " + selectedCampaign.name() + ":\n\n" +
                        IntStream.range(0, agencies.size())
                                .mapToObj(i -> (i + 1) + ". " + agencies.get(i).name())
                                .collect(Collectors.joining("\n")) +
                        "\n\nПожалуйста, выберите орган, введя его номер.";
                
                SendMessage message = new SendMessage(chatId.toString(), messageTextBuilder);

                userStates.put(chatId, "SELECTING_AGENCY");

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить список органов: {}", e.getMessage(), e);
                }
            } else {
                SendMessage message = new SendMessage(chatId.toString(), "Неверный выбор кампании. Пожалуйста, попробуйте снова.");
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                }
                sendCampaignsList(chatId);
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage(chatId.toString(), "Пожалуйста, введите корректный номер.");
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException ex) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage(), ex);
            }
            sendCampaignsList(chatId);
        }
    }
    
    private void handleAgencySelection(Long chatId, String messageText) {
        try {
            String campaignId = userSelectedCampaign.get(chatId);
            Campaign selectedCampaign = appConfig.getCampaigns().stream()
                    .filter(c -> c.id().equals(campaignId))
                    .findFirst()
                    .orElse(null);

            if (selectedCampaign != null) {
                int agencyIndex = Integer.parseInt(messageText) - 1;
                List<Agency> agencies = selectedCampaign.agencies();

                if (agencyIndex >= 0 && agencyIndex < agencies.size()) {
                    Agency selectedAgency = agencies.get(agencyIndex);
                    userSelectedAgency.put(chatId, selectedAgency.id());
                    
                    // Если нет шаблона, отправляем только инструкцию без выбора опций
                    logger.info("Проверка наличия шаблона для агентства {}: hasTemplate={} (templateFile={})",
                        selectedAgency.name(), selectedAgency.hasTemplate(), selectedAgency.templateFile());
                    if (!selectedAgency.hasTemplate()) {
                        logger.info("Агентство {} не имеет шаблона, отправляем только инструкцию", selectedAgency.name());
                        if (selectedAgency.hasInstruction()) {
                            sendInstructionOnly(chatId, selectedAgency);
                        } else {
                            sendNoContentMessage(chatId, selectedAgency);
                        }
                    } else {
                        // Если есть шаблон, показываем варианты доставки
                        List<String> deliveryOptions = selectedAgency.deliveryOptions();
                        AtomicInteger optionIndex = new AtomicInteger(1);
                        String deliveryList = deliveryOptions.stream()
                                .map(o -> switch (o) {
                                    case "file" -> "файл";
                                    case "text" -> "текстовое сообщение";
                                    default     -> o;
                                })
                                .map(display -> optionIndex.getAndIncrement() + ". " + display)
                                .collect(Collectors.joining("\n"));

                        String instructionOption = selectedAgency.hasInstruction()
                                ? "\n" + optionIndex.getAndIncrement() + ". Только инструкция"
                                : "";

                        String deliveryText = "Доступные варианты отправки шаблона для " +
                                selectedAgency.name() + ":\n\n" + deliveryList + instructionOption +
                                "\n\nПожалуйста, выберите вариант доставки, введя его номер.";
                        
                        SendMessage message = new SendMessage(chatId.toString(), deliveryText);
                        
                        userStates.put(chatId, "SELECTING_DELIVERY");
                        
                        try {
                            telegramClient.execute(message);
                        } catch (TelegramApiException e) {
                            logger.error("Не удалось отправить варианты доставки: {}", e.getMessage(), e);
                        }
                    }
                } else {
                    SendMessage message = new SendMessage(chatId.toString(), "Неверный выбор органа. Пожалуйста, попробуйте снова.");
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                    }
                    // Повторно отправить список органов
                    handleCampaignSelection(chatId, String.valueOf(
                            appConfig.getCampaigns().indexOf(selectedCampaign) + 1));

                }
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage(chatId.toString(), "Пожалуйста, введите корректный номер.");
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException ex) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage(), ex);
            }
        }
    }
    
    private void handleDeliverySelection(Long chatId, String messageText) {
        try {
            String campaignId = userSelectedCampaign.get(chatId);
            String agencyId = userSelectedAgency.get(chatId);
            
            Campaign selectedCampaign = appConfig.getCampaigns().stream()
                    .filter(c -> c.id().equals(campaignId))
                    .findFirst()
                    .orElse(null);

            if (selectedCampaign != null) {
                Agency selectedAgency = selectedCampaign.agencies().stream()
                        .filter(a -> a.id().equals(agencyId))
                        .findFirst()
                        .orElse(null);

                if (selectedAgency != null) {
                    int optionIndex = Integer.parseInt(messageText) - 1;
                    List<String> deliveryOptions = selectedAgency.deliveryOptions();
                    
                    // Проверяем, есть ли шаблон
                    boolean hasTemplate = selectedAgency.hasTemplate();
                    boolean hasInstruction = selectedAgency.hasInstruction();
                    
                    int optionCount = deliveryOptions.size();
                    if (hasInstruction && !hasTemplate) {
                        optionCount++; // Добавляем опцию "только инструкция" если нет шаблона
                    } else if (hasInstruction && hasTemplate) {
                        optionCount++; // Добавляем опцию "только инструкция" даже если есть шаблон
                    }
                    
                    if (optionIndex >= 0 && optionIndex < optionCount) {
                        // Проверяем, выбрана ли опция "только инструкция"
                        if (hasInstruction && optionIndex == deliveryOptions.size()) {
                            // Отправить только инструкцию
                            sendInstructionOnly(chatId, selectedAgency);
                        } else if (hasTemplate && optionIndex < deliveryOptions.size()) {
                            // Отправить шаблон
                            String selectedOption = deliveryOptions.get(optionIndex);
                            
                            // Преобразуем русские варианты обратно в английские для внутренней обработки
                            if ("файл".equals(selectedOption)) {
                                selectedOption = "file";
                            } else if ("текстовое сообщение".equals(selectedOption)) {
                                selectedOption = "text";
                            }
                            
                            // Увеличить статистику
                            if (selectedAgency.hasTemplate()) {
                                statisticsService.incrementTemplateDownload(selectedAgency.templateFile());
                            }
                            
                            sendTemplate(chatId, selectedAgency, selectedOption);
                        } else {
                            sendNoContentMessage(chatId, selectedAgency);
                        }
                        
                        // Сбросить состояние пользователя
                        userStates.remove(chatId);
                        userSelectedCampaign.remove(chatId);
                        userSelectedAgency.remove(chatId);
                    } else {
                        SendMessage message = new SendMessage(chatId.toString(), "Неверный вариант доставки. Пожалуйста, попробуйте снова.");
                        try {
                            telegramClient.execute(message);
                        } catch (TelegramApiException e) {
                            logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage(chatId.toString(), "Пожалуйста, введите корректный номер.");
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException ex) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage(), ex);
            }
        }
    }
    
    private void sendTemplate(Long chatId, com.utilteleg.bot.model.Agency agency, String deliveryOption) {
        // Проверяем, есть ли шаблон
        if (!agency.hasTemplate()) {
            // Если шаблона нет, отправляем только инструкцию
            sendInstructionOnly(chatId, agency);
            return;
        }
        
        if ("file".equals(deliveryOption)) {
            // Отправить как файл
            File templateFile = new File(agency.templateFile());
            if (templateFile.exists()) {
                try {
                    SendDocument sendDocument = SendDocument.builder()
                            .chatId(chatId.toString())
                            .document(new InputFile(templateFile))
                            .caption("Вот ваш шаблон заявления")
                            .build();
                    
                    telegramClient.execute(sendDocument);
                    // После отправки шаблона отправляем инструкцию
                    sendInstructionFile(chatId.toString(), agency);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить шаблон как файл: {}", e.getMessage(), e);
                    // Откат к тексту
                    sendTemplateAsText(chatId, agency);
                }
            } else {
                logger.error("Файл шаблона не найден: {}", agency.templateFile());
                sendTemplateAsText(chatId, agency);
            }
        } else {
            // Отправить как текст
            sendTemplateAsText(chatId, agency);
        }
    }
    
    private void sendTemplateAsText(Long chatId, com.utilteleg.bot.model.Agency agency) {
        // Проверяем, есть ли шаблон
        if (!agency.hasTemplate()) {
            // Если шаблона нет, отправляем только инструкцию
            sendInstructionOnly(chatId, agency);
            return;
        }
        
        File templateFile = new File(agency.templateFile());
        if (templateFile.exists()) {
            try {
                String templateContent;
                
                // Проверяем расширение файла для правильной обработки
                String fileName = templateFile.getName().toLowerCase();
                if (fileName.endsWith(".docx")) {
                    // Для DOCX файлов извлекаем текст с помощью Apache POI
                    templateContent = extractTextFromDocx(templateFile);
                } else {
                    // Для текстовых файлов читаем содержимое напрямую
                    templateContent = java.nio.file.Files.readString(templateFile.toPath());
                }
                
                SendMessage message = new SendMessage(chatId.toString(), "Вот ваш шаблон заявления:\n\n" + templateContent);
                
                try {
                    telegramClient.execute(message);
                    // После отправки шаблона отправляем инструкцию
                    sendInstructionFile(chatId.toString(), agency);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить шаблон как текст: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error("Не удалось прочитать файл шаблона: {}", e.getMessage(), e);
                
                SendMessage message = new SendMessage(chatId.toString(), "Извините, не удалось получить шаблон в данный момент.");
                
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException ex) {
                    logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), ex);
                }
            }
        } else {
            logger.error("Файл шаблона не найден: {}", agency.templateFile());
            
            SendMessage message = new SendMessage(chatId.toString(), "Извините, файл шаблона недоступен.");
            
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Извлекает текст из DOCX файла с помощью Apache POI
     * @param docxFile DOCX файл для извлечения текста
     * @return Извлеченный текст из DOCX файла
     * @throws IOException если возникает ошибка при чтении файла
     */
    private String extractTextFromDocx(File docxFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
            
            return text.toString();
        } catch (Exception e) {
            logger.error("Ошибка при извлечении текста из DOCX файла: {}", e.getMessage(), e);
            throw new IOException("Не удалось извлечь текст из DOCX файла", e);
        }
    }
    
    /**
     * Отправляет инструкцию по отправке заявления в виде текстового сообщения
     */
    private void sendInstructionFile(String chatId, Agency agency) {
        try {
            String instructionText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.name().toUpperCase() + "\n\n";
            
            // Read instruction content from file or use default text
            String instructionFilePath = agency.instructionFile();
            if (instructionFilePath != null && !instructionFilePath.isEmpty()) {
                File instructionFile = new File(instructionFilePath);
                if (instructionFile.exists()) {
                    // Read the instruction file content
                    String fileContent = new String(Files.readAllBytes(Paths.get(instructionFilePath)), StandardCharsets.UTF_8);
                    instructionText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.name().toUpperCase() + "\n\n" + fileContent;
                } else {
                    // If instruction file doesn't exist, use default instruction
                    instructionText += getDefaultInstructionText(agency);
                }
            } else {
                // If no instruction file path is specified, use default instruction
                instructionText += getDefaultInstructionText(agency);
            }
            
            // Send as simple text message
            SendMessage message = new SendMessage(chatId, instructionText);
            telegramClient.execute(message);
            logger.info("Отправлена текстовая инструкция по отправке для агентства: {}", agency.name());
        } catch (Exception e) {
            logger.error("Ошибка при отправке инструкции по отправке для агентства {}: {}", agency.name(), e.getMessage());
            // Send a simple fallback message
            try {
                String fallbackText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.name().toUpperCase() + "\n\n" +
                        "1. Подготовьте все необходимые документы\n" +
                        "2. Проверьте правильность заполнения заявления\n" +
                        "3. Выберите удобный способ подачи документов\n" +
                        "4. После отправки сохраните подтверждение\n" +
                        "5. Следите за статусом рассмотрения заявления\n\n" +
                        "Подробности уточняйте на официальном сайте " + agency.name() + ".";
                
                SendMessage message = new SendMessage(chatId, fallbackText);
                telegramClient.execute(message);
                logger.info("Отправлена резервная инструкция по отправке для агентства: {}", agency.name());
            } catch (TelegramApiException ex) {
                logger.error("Ошибка при отправке резервной инструкции для агентства {}: {}", agency.name(), ex.getMessage());
            }
        }
    }
    
    /**
     * Возвращает текст инструкции по умолчанию
     */
    private String getDefaultInstructionText(Agency agency) {
        return "1. Подготовьте все необходимые документы\n" +
                "2. Проверьте правильность заполнения заявления\n" +
                "3. Выберите удобный способ подачи документов\n" +
                "4. После отправки сохраните подтверждение\n" +
                "5. Следите за статусом рассмотрения заявления\n\n" +
                "Подробности уточняйте на официальном сайте " + agency.name() + ".";
    }
    
    /**
     * Отправляет только инструкцию без шаблона
     */
    private void sendInstructionOnly(Long chatId, Agency agency) {
        sendInstructionFile(chatId.toString(), agency);
        
        // Сбросить состояние пользователя
        userStates.remove(chatId);
        userSelectedCampaign.remove(chatId);
        userSelectedAgency.remove(chatId);
    }
    
    /**
     * Отправляет сообщение о том, что нет контента для отправки
     */
    private void sendNoContentMessage(Long chatId, Agency agency) {
        SendMessage message = new SendMessage(chatId.toString(), "Извините, для данного органа нет доступных шаблонов или инструкций.");
        
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить сообщение об отсутствии контента: {}", e.getMessage(), e);
        }
        
        // Сбросить состояние пользователя
        userStates.remove(chatId);
        userSelectedCampaign.remove(chatId);
        userSelectedAgency.remove(chatId);
    }
}