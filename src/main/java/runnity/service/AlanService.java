package runnity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import runnity.dto.ChatRequest;
import runnity.dto.ChatResponse;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AlanService {
    // 외부 HTTP API 호출용 스프링 기본 클래스.
    private final RestTemplate restTemplate;

    // yaml 값 주입
    @Value("${ai.api.base-url}")
    private String apiBaseUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Autowired
    public AlanService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ChatRequest createRequest(String request){
        ChatRequest chatRequest = new ChatRequest(request);
        return chatRequest;
    }

    public ChatResponse getResonse(ChatRequest request) {
        // 쿼리 구성
        String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/api/v1/question")
                .queryParam("content", request.getMessage())  // ChatRequest에 들어있는 사용자의 질문
                .queryParam("client_id", apiKey)
                .toUriString(); // 실제 endpoint

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        // API 호출
        ResponseEntity<ChatResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ChatResponse.class
        );
        return response.getBody();
    }

    public void getWeatherAI() {
//        ChatRequest req = new ChatRequest();
//        req.setMessage("서울 날씨를 요약해서 알려줘."); // 하드코딩 프롬프트
//
//        ChatResponse res = chat(req);
//        return res != null ? res.getMessage() : "AI 응답 없음";
    }


    public void updateLocationInfo() {
    }

    public void updateWeatherInfo() {
    }


}
