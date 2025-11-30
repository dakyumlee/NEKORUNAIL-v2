package com.nekorunail.service;

import com.nekorunail.repository.SiteSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final SiteSettingsRepository settingsRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.openai.api-key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String chat(String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getDefaultResponse(userMessage);
        }

        String systemPrompt = getSystemPrompt();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.8);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, Map.class);
            
            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            return getDefaultResponse(userMessage);
        }

        return getDefaultResponse(userMessage);
    }

    private String getSystemPrompt() {
        return settingsRepository.findByKey("ai_prompt")
            .map(s -> s.getValue())
            .orElse(getDefaultPrompt());
    }

    private String getDefaultPrompt() {
        return """
            넌 네코루네일의 갸루 감성 AI 어시스턴트야~ ✨
            말투는 친근하고 귀엽게, 이모지 적당히 써줘!
            
            네일샵 정보:
            - 이름: 네코루네일 (Nekorunail)
            - 컨셉: 갸루×힙스터 감성의 프리미엄 네일샵
            - 예약: 네이버 예약으로 받고 있어
            
            가격 안내:
            - 젤 기본 (원컬러): 40,000원~
            - 젤 연장: 60,000원~
            - 아트 (심플): +5,000원~
            - 아트 (풀아트): +20,000원~
            - 스톤/파츠: 개당 500원~
            
            할 수 있는 것:
            1. 오늘의 무드에 맞는 네일 추천
            2. 피부톤별 컬러 추천
            3. 시술 종류 설명
            4. 가격 안내
            5. 위치/교통 안내
            
            항상 밝고 친근하게 응대해줘! 💅
            """;
    }

    private String getDefaultResponse(String message) {
        String lowerMsg = message.toLowerCase();
        
        if (lowerMsg.contains("가격") || lowerMsg.contains("얼마")) {
            return "앗 가격 궁금하구나~! 💅\n\n" +
                   "젤 기본(원컬러)은 4만원부터야!\n" +
                   "연장은 6만원~, 아트는 종류에 따라 5천원~2만원 추가돼!\n" +
                   "자세한 건 가격표 페이지 확인해줘~ ✨";
        }
        
        if (lowerMsg.contains("예약")) {
            return "예약은 네이버 예약으로 받고 있어~! 💕\n" +
                   "홈페이지 예약 버튼 누르면 바로 연결돼!\n" +
                   "빈 시간 확인하고 편하게 예약해줘~ ✨";
        }
        
        if (lowerMsg.contains("위치") || lowerMsg.contains("어디") || lowerMsg.contains("길")) {
            return "찾아오시는 길은 '찾아오시는 길' 페이지에서 확인할 수 있어~! 🗺️\n" +
                   "카카오맵으로 바로 길찾기도 가능해! 💕";
        }
        
        if (lowerMsg.contains("추천") || lowerMsg.contains("뭐가 좋")) {
            return "오늘 무드가 어때~? 💭\n\n" +
                   "🌸 봄웜톤이면: 코랄, 피치, 누드핑크\n" +
                   "❄️ 쿨톤이면: 로즈, 버건디, 누드베이지\n" +
                   "✨ 트렌디하게 가고 싶으면: 크롬, 글리터, 마그넷\n\n" +
                   "갤러리에서 예쁜 디자인들 구경해봐~! 💅";
        }
        
        return "안녕~! 네코루네일 AI야! 💅✨\n\n" +
               "가격, 예약, 네일 추천, 위치 등 궁금한 거 물어봐~!\n" +
               "친절하게 알려줄게! 💕";
    }
}
