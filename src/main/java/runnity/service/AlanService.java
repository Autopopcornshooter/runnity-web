package runnity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import runnity.dto.ChatRequest;
import runnity.dto.ChatResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AlanService {
    // 외부 HTTP API 호출용 스프링 기본 클래스.
    private final RestTemplate restTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;

    // yaml 값 주입
    @Value("${ai.api.base-url}")
    private String apiBaseUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Autowired
    public AlanService(RestTemplate restTemplate, CustomOAuth2UserService customOAuth2UserService) {
        this.restTemplate = restTemplate;
        this.customOAuth2UserService = customOAuth2UserService;
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
                .toUriString();

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

    private String loadPrompt(String filePath, String userLocation) {
        String template;
        try (InputStream is = new ClassPathResource(filePath).getInputStream()) {
            template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return template.replace("{LOCATION}", userLocation);
    }

    public String getWeatherPrompt() {
        String userLocation = "대전광역시 서구"; // 로그인 유저 위치
        return loadPrompt("prompts/weather.txt", userLocation);
    }

    public String getLocationPrompt() {
        String userLocation = "서울특별시 종로구";
        return loadPrompt("prompts/location.txt", userLocation);
    }

    public Map<String, Object> weatherNomaliztion(ChatResponse weatherResponse) {
        Map<String, Object> weatherData = new LinkedHashMap<>();

        // Alan API 답변 내용 확인
        String content = weatherResponse.getContent();
        if (content == null) {
            weatherData.put("error", "AI 응답 없음");
            return weatherData;
        }

        weatherData.put("presentWeather", extractWeatherValue(content, "현재 날씨"));
        weatherData.put("temperature", extractWeatherValue(content, "온도"));
        weatherData.put("humidity", extractWeatherValue(content, "습도"));
        weatherData.put("percentage", extractWeatherValue(content, "강수확률"));
        weatherData.put("precipitation", extractWeatherValue(content, "강수량"));
        weatherData.put("highLow", extractWeatherValue(content, "최고/최저 기온"));
        weatherData.put("dust", extractWeatherValue(content, "미세먼지"));
        weatherData.put("weatherSummary", extractWeatherValue(content, "한줄 요약"));

        // 시간별 요약 리스트
        List<Map<String, String>> hourlyList = new ArrayList<>();
        Pattern p = Pattern.compile("- \\*\\*(\\d+)시간 후\\*\\*:\\s*온도\\s*([\\d.]+)°C,\\s*강수확률\\s*(\\d+)%");
        Matcher m = p.matcher(content);
        while (m.find()) {
            Map<String, String> hour = new LinkedHashMap<>();
            hour.put("hourly", m.group(1) + "시간 후");
            hour.put("temperature", m.group(2) + "°C");
            hour.put("percentage", m.group(3) + "%");
            hourlyList.add(hour);
        }
        weatherData.put("hourlyList", hourlyList);

        System.out.println("=== 정규화된 날씨 데이터 ===");
        weatherData.forEach((k, v) -> System.out.println(k + " : " + v));

        return weatherData;
    }

    public List<Map<String, Object>> locationNomaliztion(ChatResponse locationResponse) {
        List<Map<String, Object>> locationList = new ArrayList<>();

        String content = locationResponse.getContent();
        if (content == null || content.isEmpty()) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "AI 응답 없음");
            locationList.add(errorMap);
            return locationList;
        }

        // 코스명에 따라 분리
        String[] courses = content.split("(?=-\\s*\\*\\*장소 명칭\\*\\*\\s*:)");

        for(String course: courses) {
//            if(course.isEmpty()) continue;
            if(course.isEmpty() || !course.contains("장소 명칭")) continue; // null 코스 제거

            Map<String, Object> courseData = new LinkedHashMap<>();
            courseData.put("courseName", extractLocationValue(course, "장소 명칭"));
            courseData.put("courseLength", extractLocationValue(course, "코스 길이"));
            courseData.put("recommend", extractLocationValue(course, "추천 수 "));
            courseData.put("courseInfo", extractLocationValue(course, "소개"));

            locationList.add(courseData);
        }


        System.out.println("=== 정규화된 정보 데이터 ===");
        locationList.forEach(System.out::println);

        return locationList;
    }
    private String extractWeatherValue(String input, String field) {
//        Pattern pattern = Pattern.compile("\\*\\*" + field + "\\*\\*:\\s*([^\\n]+)");
        Pattern pattern = Pattern.compile("(?m)^\\s*-\\s*\\*\\*" + Pattern.quote(field.trim()) + "\\*\\*\\s*:\\s*(.+)$");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
    private String extractLocationValue(String input, String field) {
//        Pattern pattern = Pattern.compile("\\*\\*" + field + "\\*\\*:\\s*([^\\n]+)");
        Pattern pattern = Pattern.compile("(?m)^\\s*-\\s*\\*\\*\\s*" + Pattern.quote(field.trim()) + "\\s*\\*\\*\\s*:\\s*(.+)$");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}
