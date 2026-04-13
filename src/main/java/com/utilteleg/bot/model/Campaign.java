package com.utilteleg.bot.model;

import java.util.List;

public record Campaign(
        String id,
        String name,
        List<Agency> agencies
) {}
