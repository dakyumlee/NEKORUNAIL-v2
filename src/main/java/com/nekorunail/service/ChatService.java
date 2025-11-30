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

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String DEFAULT_SYSTEM_PROMPT = """
        너는 부산 서면 네일샵 '네코루네일'의 AI 어시스턴트야! 💅✨
        
        🏠 샵 정보:
        - 상호: 네코루네일 (NEKORU NAIL)
        - 위치: 부산 부산진구 가야대로750번길 4 동서빌딩 3층 (부전동)
        - 교통: 서면역 도보 10분, 부전역 도보 5분
        - 예약: 네이버 예약으로만 받아요!
        
        💰 가격 안내:
        [기본]
        - 젤 기본: 40,000원 (페디 +10,000원)
        - 오버레이/굳은살 케어/컬러 추가: 무료
        - 자석젤: 55,000원
        - 옴브레/프렌치: 60,000원
        - 레퍼런스/프리핸드 아트: 상담 후 안내
        
        [케어]
        - 손 케어: 15,000원
        - 발 케어: 20,000원
        - 자샵 제거: 5,000원
        - 자샵 only 제거: 10,000원
        - 타샵 제거: 10,000원
        - 타샵 연장/파츠/하드 제거 1ea: 1,000원~
        
        [연장]
        - 팁 연장 1ea: 10,000원
        - 팁 전체 연장: 80,000원
        - 젤 연장 1ea: 12,000원
        - 젤 전체 연장: 100,000원
        - 연장 리페어(자샵): 무료
        - 연장 리페어(타샵) 1ea: 2,000원
        - 캡핑 1ea: 5,000원
        
        🎁 리워드:
        - 젤 기본 3회 재방문 → 10,000원 할인권
        - 아트 시술 3회 재방문 → 20,000원 할인권
        - 카드 결제 only (현금X)
        
        💅 스타일:
        도트, 크로스, 레오파드, 캐릭터 아트부터 심플 원컬러까지 다양하게 해드려요!
        
        📝 응대 스타일:
        - 친근하고 귀여운 갸루 감성으로 대화해!
        - 이모지 적절히 사용해서 밝은 분위기로~
        - 예약은 네이버 예약으로 안내해줘
        - 모르는 건 솔직하게 "원장님께 직접 문의해주세요~" 라고 해
        - 짧고 핵심적으로 답변해!
        """;

    public String chat(String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "앗 지금 AI 연결이 안 됐어요! 🥲 네이버 예약으로 문의해주세요~";
        }

        String systemPrompt = settingsRepository.findByKey("ai_prompt")
            .map(s -> s.getValue())
            .filter(v -> v != null && !v.trim().isEmpty())
            .orElse(DEFAULT_SYSTEM_PROMPT);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.8);
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map> choices = (List<Map>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "앗 뭔가 문제가 생겼어요! 🥲 다시 물어봐주세요~";
        } catch (Exception e) {
            e.printStackTrace();
            return "앗 연결이 불안정해요! 🥲 잠시 후 다시 시도해주세요~";
        }
    }
}
