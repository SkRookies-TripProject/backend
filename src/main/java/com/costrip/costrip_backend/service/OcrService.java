package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.receipt.ReceiptDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OcrService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("anthropic-version", "2023-06-01")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReceiptDto analyze(String base64Image) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", "claude-opus-4-5");
            root.put("max_tokens", 1024);

            ArrayNode messages = root.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");

            ArrayNode content = message.putArray("content");

            ObjectNode imageBlock = content.addObject();
            imageBlock.put("type", "image");
            ObjectNode source = imageBlock.putObject("source");
            source.put("type", "base64");
            source.put("media_type", detectMediaType(base64Image));
            source.put("data", base64Image);

            ObjectNode textBlock = content.addObject();
            textBlock.put("type", "text");
            textBlock.put("text",
                    "이 영수증 이미지에서 정보를 추출해서 JSON 형식으로만 응답해줘. 다른 설명 없이 JSON만.\n" +
                            "{\n" +
                            "  \"storeName\": \"가게명\",\n" +
                            "  \"date\": \"날짜 (YYYY-MM-DD)\",\n" +
                            "  \"items\": [{\"name\": \"상품명\", \"price\": 숫자}],\n" +
                            "  \"subtotal\": 숫자,\n" +
                            "  \"tax\": 숫자,\n" +
                            "  \"total\": 숫자\n" +
                            "}\n" +
                            "값을 알 수 없으면 null로 표시해줘."
            );

            String requestBody = objectMapper.writeValueAsString(root);

            String response = webClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> new RuntimeException("Claude API 오류: " + errorBody)))
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("Claude API 호출 실패: " + e.getMessage());
        }
    }

    private ReceiptDto parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("content").get(0).path("text").textValue();
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}") + 1;
            String json = text.substring(start, end);
            return objectMapper.readValue(json, ReceiptDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Claude 응답 파싱 실패: " + e.getMessage());
        }
    }

    private String detectMediaType(String base64) {
        try {
            byte[] header = java.util.Base64.getDecoder().decode(base64.substring(0, 12));
            if (header[0] == (byte) 0x89 && header[1] == 0x50
                    && header[2] == 0x4E && header[3] == 0x47) return "image/png";
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) return "image/jpeg";
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46) return "image/gif";
            if (header[0] == 0x52 && header[1] == 0x49
                    && header[2] == 0x46 && header[3] == 0x46) return "image/webp";
        } catch (Exception ignored) {}
        return "image/jpeg";
    }
}