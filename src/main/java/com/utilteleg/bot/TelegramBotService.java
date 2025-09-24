package main.java.com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import com.utilteleg.bot.model.Campaign;
import com.utilteleg.bot.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBotService extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    
    private final String botUsername;
    private final String botToken;
    
    // В реальном приложении вы бы использовали правильную структуру данных или базу данных
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> userSelectedCampaign = new ConcurrentHashMap<>();
    private final Map<Long, String> userSelectedAgency = new ConcurrentHashMap<>();
    
    @Autowired
    private com.utilteleg.bot.config.AppConfig appConfig;
    
    @Autowired
    private StatisticsService statisticsService;
    
    public TelegramBotService(
            @Value("${telegram.bot.username}") String botUsername,
            @Value("${telegram.bot.token}") String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
    }
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
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
        String welcomeText = "Добро пожаловать в бот шаблонов государственных заявлений!\n" +
                "Используйте /campaigns для просмотра доступных кампаний.";
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(welcomeText);
        
        try {
            execute(message);
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
            SendMessage errorMessage = new SendMessage();
            errorMessage.setChatId(chatId.toString());
            errorMessage.setText("Извините, произошла критическая ошибка при загрузке конфигурации. AppConfig недоступен.\n\n" +
                               "Диагностика:\n" +
                               "- AppConfig бин равен null\n" +
                               "- Конфигурация не была внедрена правильно\n" +
                               "- Проверьте аннотации класса AppConfig и Application");
            try {
                execute(errorMessage);
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
            
            SendMessage errorMessage = new SendMessage();
            errorMessage.setChatId(chatId.toString());
            errorMessage.setText("Извините, произошла ошибка при загрузке конфигурации. Список кампаний недоступен.\n\n" +
                               "Диагностика:\n" +
                               "- Список кампаний равен null\n" +
                               "- Статус конфигурации: " + configStatus + "\n" +
                               "- Проверьте файл application.yml\n" +
                               "- Проверьте правильность формата YAML\n" +
                               "- Убедитесь, что путь к файлу шаблона существует");
            try {
                execute(errorMessage);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
            }
            return;
        }
        
        if (campaigns.isEmpty()) {
            logger.warn("Список кампаний пуст. Проверьте файл конфигурации application.yml.");
            SendMessage warningMessage = new SendMessage();
            warningMessage.setChatId(chatId.toString());
            warningMessage.setText("Список доступных кампаний пуст. Пожалуйста, обратитесь к администратору бота.\n\n" +
                                 "Диагностика:\n" +
                                 "- Список кампаний пуст (0 элементов)\n" +
                                 "- Проверьте содержимое файла application.yml");
            try {
                execute(warningMessage);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить предупреждающее сообщение: {}", e.getMessage(), e);
            }
            return;
        }
        
        logger.info("Успешно получено {} кампаний из конфигурации", campaigns.size());
        StringBuilder messageText = new StringBuilder("Доступные кампании:\n\n");
        
        for (int i = 0; i < campaigns.size(); i++) {
            Campaign campaign = campaigns.get(i);
            messageText.append((i + 1)).append(". ").append(campaign.getName())
                    .append(" - ").append(campaign.getDescription()).append("\n");
        }
        
        messageText.append("\nПожалуйста, выберите кампанию, введя её номер.");
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(messageText.toString());
        
        userStates.put(chatId, "SELECTING_CAMPAIGN");
        
        try {
            execute(message);
            logger.info("Список кампаний успешно отправлен пользователю");
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить список кампаний: {}", e.getMessage(), e);
        }
    }
    
    private void handleUserResponse(Long chatId, String messageText) {
        String state = userStates.get(chatId);
        
        switch (state) {
            case "SELECTING_CAMPAIGN":
                handleCampaignSelection(chatId, messageText);
                break;
            case "SELECTING_AGENCY":
                handleAgencySelection(chatId, messageText);
                break;
            case "SELECTING_DELIVERY":
                handleDeliverySelection(chatId, messageText);
                break;
            default:
                sendWelcomeMessage(chatId);
                break;
        }
    }
    
    private void handleCampaignSelection(Long chatId, String messageText) {
        try {
            int campaignIndex = Integer.parseInt(messageText) - 1;
            List<Campaign> campaigns = appConfig.getCampaigns();
            
            if (campaignIndex >= 0 && campaignIndex < campaigns.size()) {
                Campaign selectedCampaign = campaigns.get(campaignIndex);
                userSelectedCampaign.put(chatId, selectedCampaign.getId());
                
                StringBuilder messageTextBuilder = new StringBuilder("Доступные органы для ")
                        .append(selectedCampaign.getName()).append(":\n\n");
                
                List<com.utilteleg.bot.model.Agency> agencies = selectedCampaign.getAgencies();
                for (int i = 0; i < agencies.size(); i++) {
                    com.utilteleg.bot.model.Agency agency = agencies.get(i);
                    messageTextBuilder.append((i + 1)).append(". ").append(agency.getName())
                            .append(" - ").append(agency.getDescription()).append("\n");
                }
                
                messageTextBuilder.append("\nПожалуйста, выберите орган, введя его номер.");
                
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText(messageTextBuilder.toString());
                
                userStates.put(chatId, "SELECTING_AGENCY");
                
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить список органов: {}", e.getMessage(), e);
                }
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("Неверный выбор кампании. Пожалуйста, попробуйте снова.");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                }
                sendCampaignsList(chatId);
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Пожалуйста, введите корректный номер.");
            try {
                execute(message);
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
                    .filter(c -> c.getId().equals(campaignId))
                    .findFirst()
                    .orElse(null);
            
            if (selectedCampaign != null) {
                int agencyIndex = Integer.parseInt(messageText) - 1;
                List<com.utilteleg.bot.model.Agency> agencies = selectedCampaign.getAgencies();
                
                if (agencyIndex >= 0 && agencyIndex < agencies.size()) {
                    com.utilteleg.bot.model.Agency selectedAgency = agencies.get(agencyIndex);
                    userSelectedAgency.put(chatId, selectedAgency.getId());
                    
                    StringBuilder messageTextBuilder = new StringBuilder("Доступные варианты доставки для ")
                            .append(selectedAgency.getName()).append(":\n\n");
                    
                    List<String> deliveryOptions = selectedAgency.getDeliveryOptions();
                    for (int i = 0; i < deliveryOptions.size(); i++) {
                        messageTextBuilder.append((i + 1)).append(". ").append(deliveryOptions.get(i)).append("\n");
                    }
                    
                    messageTextBuilder.append("\nПожалуйста, выберите вариант доставки, введя его номер.");
                    
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId.toString());
                    message.setText(messageTextBuilder.toString());
                    
                    userStates.put(chatId, "SELECTING_DELIVERY");
                    
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        logger.error("Не удалось отправить варианты доставки: {}", e.getMessage(), e);
                    }
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId.toString());
                    message.setText("Неверный выбор органа. Пожалуйста, попробуйте снова.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                    }
                    // Повторно отправить список органов
                    handleCampaignSelection(chatId, String.valueOf(
                            appConfig.getCampaigns().indexOf(selectedCampaign) + 1));
                }
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Пожалуйста, введите корректный номер.");
            try {
                execute(message);
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
                    .filter(c -> c.getId().equals(campaignId))
                    .findFirst()
                    .orElse(null);
            
            if (selectedCampaign != null) {
                com.utilteleg.bot.model.Agency selectedAgency = selectedCampaign.getAgencies().stream()
                        .filter(a -> a.getId().equals(agencyId))
                        .findFirst()
                        .orElse(null);
                
                if (selectedAgency != null) {
                    int optionIndex = Integer.parseInt(messageText) - 1;
                    List<String> deliveryOptions = selectedAgency.getDeliveryOptions();
                    
                    if (optionIndex >= 0 && optionIndex < deliveryOptions.size()) {
                        String selectedOption = deliveryOptions.get(optionIndex);
                        
                        // Увеличить статистику
                        statisticsService.incrementTemplateDownload(selectedAgency.getTemplateFile());
                        
                        // Отправить шаблон
                        sendTemplate(chatId, selectedAgency, selectedOption);
                        
                        // Сбросить состояние пользователя
                        userStates.remove(chatId);
                        userSelectedCampaign.remove(chatId);
                        userSelectedAgency.remove(chatId);
                    } else {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId.toString());
                        message.setText("Неверный вариант доставки. Пожалуйста, попробуйте снова.");
                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Пожалуйста, введите корректный номер.");
            try {
                execute(message);
            } catch (TelegramApiException ex) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage(), ex);
            }
        }
    }
    
    private void sendTemplate(Long chatId, com.utilteleg.bot.model.Agency agency, String deliveryOption) {
        if ("file".equals(deliveryOption)) {
            // Отправить как файл
            File templateFile = new File(agency.getTemplateFile());
            if (templateFile.exists()) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(chatId.toString());
                sendDocument.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(templateFile));
                sendDocument.setCaption("Вот ваш шаблон заявления");
                
                try {
                    execute(sendDocument);
                    // После отправки шаблона отправляем инструкцию
                    sendInstructionFile(chatId.toString(), agency);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить шаблон как файл: {}", e.getMessage(), e);
                    // Откат к тексту
                    sendTemplateAsText(chatId, agency);
                }
            } else {
                logger.error("Файл шаблона не найден: {}", agency.getTemplateFile());
                sendTemplateAsText(chatId, agency);
            }
        } else {
            // Отправить как текст
            sendTemplateAsText(chatId, agency);
        }
    }
    
    private void sendTemplateAsText(Long chatId, com.utilteleg.bot.model.Agency agency) {
        File templateFile = new File(agency.getTemplateFile());
        if (templateFile.exists()) {
            try {
                String templateContent = java.nio.file.Files.readString(templateFile.toPath());
                
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("Вот ваш шаблон заявления:\n\n" + templateContent);
                
                try {
                    execute(message);
                    // После отправки шаблона отправляем инструкцию
                    sendInstructionFile(chatId.toString(), agency);
                } catch (TelegramApiException e) {
                    logger.error("Не удалось отправить шаблон как текст: {}", e.getMessage(), e);
                }
            } catch (java.io.IOException e) {
                logger.error("Не удалось прочитать файл шаблона: {}", e.getMessage(), e);
                
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("Извините, не удалось получить шаблон в данный момент.");
                
                try {
                    execute(message);
                } catch (TelegramApiException ex) {
                    logger.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage(), ex);
                }
            }
        } else {
            logger.error("Файл шаблона не найден: {}", agency.getTemplateFile());
            
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Извините, файл шаблона недоступен.");
            
            try {
                execute(message);
            } catch (TelegramApiException e) {
                logger.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Отправляет инструкцию по отправке заявления в виде текстового сообщения
     */
    private void sendInstructionFile(String chatId, Agency agency) {
        try {
            String instructionText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.getName().toUpperCase() + "\n\n";
            
            // Read instruction content from file or use default text
            String instructionFilePath = agency.getInstructionFile();
            if (instructionFilePath != null && !instructionFilePath.isEmpty()) {
                File instructionFile = new File(instructionFilePath);
                if (instructionFile.exists()) {
                    // Read the instruction file content
                    String fileContent = new String(Files.readAllBytes(Paths.get(instructionFilePath)), StandardCharsets.UTF_8);
                    instructionText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.getName().toUpperCase() + "\n\n" + fileContent;
                } else {
                    // If instruction file doesn't exist, use default instruction
                    instructionText += getDefaultInstructionText(agency);
                }
            } else {
                // If no instruction file path is specified, use default instruction
                instructionText += getDefaultInstructionText(agency);
            }
            
            // Send as simple text message
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(instructionText);
            execute(message);
            logger.info("Отправлена текстовая инструкция по отправке для агентства: {}", agency.getName());
        } catch (Exception e) {
            logger.error("Ошибка при отправке инструкции по отправке для агентства {}: {}", agency.getName(), e.getMessage());
            // Send a simple fallback message
            try {
                String fallbackText = "ИНСТРУКЦИЯ ПО ОТПРАВКЕ ЗАЯВЛЕНИЯ В " + agency.getName().toUpperCase() + "\n\n" +
                        "1. Подготовьте все необходимые документы\n" +
                        "2. Проверьте правильность заполнения заявления\n" +
                        "3. Выберите удобный способ подачи документов\n" +
                        "4. После отправки сохраните подтверждение\n" +
                        "5. Следите за статусом рассмотрения заявления\n\n" +
                        "Подробности уточняйте на официальном сайте " + agency.getName() + ".";
                
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(fallbackText);
                execute(message);
                logger.info("Отправлена резервная инструкция по отправке для агентства: {}", agency.getName());
            } catch (TelegramApiException ex) {
                logger.error("Ошибка при отправке резервной инструкции для агентства {}: {}", agency.getName(), ex.getMessage());
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
                "Подробности уточняйте на официальном сайте " + agency.getName() + ".";
    }
}