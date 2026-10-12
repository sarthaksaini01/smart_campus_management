package com.smartcampus.smart_campus.ai;

import java.util.function.Supplier;

public record ChatTool(String name, String description, Supplier<String> handler) {}