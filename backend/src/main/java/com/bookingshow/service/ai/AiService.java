package com.bookingshow.service.ai;

import com.bookingshow.dto.AiSearchRequest;
import com.bookingshow.dto.EventResponse;
import com.bookingshow.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final EventService eventService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String modelName;

    @Cacheable(value = "aiSearchCache", key = "#naturalQuery")
    public List<EventResponse> searchByNaturalLanguage(String naturalQuery) {
        try {
            log.info("🤖 AI Search Request (Groq): {}", naturalQuery);

            String response = callGroqApi(naturalQuery);
            AiSearchRequest aiRequest = parseResponse(response);

            log.info("📊 Parsed → Keyword: '{}' | Category: {} | Start: {} | End: {}",
                    aiRequest.getKeyword(), aiRequest.getCategory(),
                    aiRequest.getStartDate(), aiRequest.getEndDate());

            List<EventResponse> results = executeSearch(aiRequest);

            // Fallback mạnh hơn
            if (results.isEmpty()) {
                log.info("🔄 Fallback search activated");
                if (aiRequest.getCategory() != null) {
                    results = eventService.searchEvents(null, aiRequest.getCategory(), null, null, PageRequest.of(0, 50)).getContent();
                } else if (aiRequest.getKeyword() != null) {
                    results = eventService.searchByKeyword(aiRequest.getKeyword());
                }
            }

            log.info("✅ Groq trả về {} sự kiện", results.size());
            return results;

        } catch (Exception e) {
            log.error("❌ Groq AI failed", e);
            return getFallbackResults();
        }
    }

    private String callGroqApi(String query) {
        var requestBody = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", query)
                ),
                "temperature", 0.2,
                "max_tokens", 700
        );

        RestClient restClient = RestClient.create();

        Map<String, Object> response = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        @SuppressWarnings("unchecked")
        var choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
        @SuppressWarnings("unchecked")
        var message = (Map<String, Object>) choice.get("message");

        return (String) message.get("content");
    }

    private String buildSystemPrompt() {
        return """
            Bạn là trợ lý tìm kiếm sự kiện cho Bookingshow.
            Trả về **CHỈ JSON**, không thêm bất kỳ chữ nào khác.

            {
              "keyword": "từ khóa hoặc null",
              "category": "MUSIC|SPORTS|THEATRE|CONFERENCE|MOVIE|OTHER hoặc null",
              "startDate": "2026-06-XXT00:00:00Z hoặc null",
              "endDate": "2026-06-XXT23:59:59Z hoặc null"
            }
            """;
    }

    private AiSearchRequest parseResponse(String response) throws Exception {
        String clean = response.replace("```json", "").replace("```", "").trim();
        return objectMapper.readValue(clean, AiSearchRequest.class);
    }

    private List<EventResponse> executeSearch(AiSearchRequest req) {
        return eventService.searchEvents(
                req.getKeyword(),
                req.getCategory(),
                req.getStartDate(),
                req.getEndDate(),
                PageRequest.of(0, 100)
        ).getContent();
    }

    private List<EventResponse> getFallbackResults() {
        log.warn("⚠️ Fallback: Trả về tất cả events");
        return eventService.getAllEvents().stream().limit(20).toList();
    }
}