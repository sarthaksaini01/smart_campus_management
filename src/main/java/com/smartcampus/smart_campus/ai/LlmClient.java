package com.smartcampus.smart_campus.ai;

import com.smartcampus.smart_campus.dto.ChatTurn;

import java.util.List;

public interface LlmClient {

    String generate(String systemPrompt, List<ChatTurn> turns, List<ChatTool> tools);

    default String generate(String systemPrompt, List<ChatTurn> turns) {
        return generate(systemPrompt, turns, List.of());
    }
}