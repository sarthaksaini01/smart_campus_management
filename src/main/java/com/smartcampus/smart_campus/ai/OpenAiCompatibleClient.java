package com.smartcampus.smart_campus.ai;

import com.smartcampus.smart_campus.dto.ChatTurn;
import com.smartcampus.smart_campus.exception.LlmUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleClient.class);

    /** The model may call tools at most this many rounds before it must answer. */
    private static final int MAX_STEPS = 4;
    private static final int MAX_TOOL_RESULT_CHARS = 6000;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleClient(
            @Value("${llm.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${llm.api-key:}") String apiKey,
            @Value("${llm.model:openai/gpt-oss-120b}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.trim())
                .defaultHeader("User-Agent", "smart-campus/1.0")
                .build();
        this.apiKey = apiKey == null ? "" : apiKey.trim().replace("\"", "").replace("'", "");
        this.model = model.trim();
        log.info("LLM client ready: baseUrl={}, model={}, apiKeyLength={}",
                baseUrl.trim(), this.model, this.apiKey.length());
    }

    @Override
    public String generate(String systemPrompt, List<ChatTurn> turns, List<ChatTool> tools) {
        if (apiKey.isBlank()) {
            throw new LlmUnavailableException("AI assistant is not configured");
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        for (ChatTurn turn : turns) {
            String role = "model".equals(turn.role()) ? "assistant" : turn.role();
            messages.add(message(role, turn.text()));
        }

        Map<String, ChatTool> byName = new LinkedHashMap<>();
        List<Map<String, Object>> toolDefinitions = new ArrayList<>();
        for (ChatTool tool : tools) {
            byName.put(tool.name(), tool);
            toolDefinitions.add(definition(tool));
        }

        for (int step = 0; step < MAX_STEPS; step++) {
            // On the last step tools are switched off, so the model has to give a final answer.
            boolean allowTools = !toolDefinitions.isEmpty() && step < MAX_STEPS - 1;
            Map<String, Object> reply = call(messages, allowTools ? toolDefinitions : List.of());

            List<Map<String, Object>> toolCalls = toolCalls(reply);
            if (toolCalls.isEmpty()) {
                return text(reply);
            }

            Map<String, Object> assistant = new LinkedHashMap<>();
            assistant.put("role", "assistant");
            assistant.put("content", reply.get("content") instanceof String s ? s : "");
            assistant.put("tool_calls", toolCalls);
            messages.add(assistant);

            for (Map<String, Object> toolCall : toolCalls) {
                messages.add(runTool(toolCall, byName));
            }
        }
        throw new LlmUnavailableException("The AI could not finish your request. Please try again");
    }

    private Map<String, Object> call(List<Map<String, Object>> messages,
                                     List<Map<String, Object>> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.3);
        if (!tools.isEmpty()) {
            body.put("tools", tools);
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            return firstMessage(response);
        } catch (RestClientResponseException e) {
            log.warn("LLM API error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                throw new LlmUnavailableException("The AI is busy right now. Please try again in a minute");
            }
            throw new LlmUnavailableException("The AI service returned an error. Please try again later");
        } catch (RestClientException e) {
            log.warn("Could not reach the LLM API: {}", e.getMessage());
            throw new LlmUnavailableException("Could not reach the AI service. Please try again later");
        }
    }

    private Map<String, Object> runTool(Map<String, Object> toolCall, Map<String, ChatTool> byName) {
        String name = null;
        if (toolCall.get("function") instanceof Map<?, ?> function
                && function.get("name") instanceof String n) {
            name = n;
        }

        ChatTool tool = name == null ? null : byName.get(name);
        String result;
        if (tool == null) {
            result = "Error: unknown tool";
        } else {
            log.info("LLM tool call: {}", name);
            try {
                result = tool.handler().get();
            } catch (RuntimeException e) {
                log.warn("Tool {} failed: {}", name, e.getMessage());
                result = "Error: " + e.getMessage();
            }
        }
        if (result.length() > MAX_TOOL_RESULT_CHARS) {
            result = result.substring(0, MAX_TOOL_RESULT_CHARS) + " ...(truncated)";
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "tool");
        message.put("tool_call_id", toolCall.get("id"));
        message.put("content", result);
        return message;
    }

    private static Map<String, Object> definition(ChatTool tool) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", new LinkedHashMap<String, Object>());

        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.put("parameters", parameters);

        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("type", "function");
        definition.put("function", function);
        return definition;
    }

    private static Map<String, Object> message(String role, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstMessage(Map<String, Object> response) {
        String failure = "The AI could not produce a response. Please rephrase your question";
        if (response == null) {
            throw new LlmUnavailableException(failure);
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new LlmUnavailableException(failure);
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new LlmUnavailableException(failure);
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toolCalls(Map<String, Object> message) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (message.get("tool_calls") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
        }
        return result;
    }

    private String text(Map<String, Object> message) {
        if (message.get("content") instanceof String content && !content.isBlank()) {
            return content.trim();
        }
        throw new LlmUnavailableException("The AI could not produce a response. Please rephrase your question");
    }
}