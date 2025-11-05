package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import runnity.dto.ChatRequest;
import runnity.dto.ChatResponse;
import runnity.service.AlanService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alan")
@RequiredArgsConstructor
public class AlanController {

    private final AlanService alanService;

    @GetMapping("/chat/weather")
    public Map<String, String> getWeatherInfo(Model model) {
        String template = null;
        try {
            template = Files.readString(
                    new ClassPathResource("prompts/weather.txt").getFile().toPath(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String userLocation = "대전광역시 유성구"; // 로그인 유저 위치
        String prompt = template.replace("{LOCATION}", userLocation);

        ChatRequest weatherRequest = alanService.createRequest(prompt);
//        ChatRequest weatherRequest = alanService.createRequest("대전광역시 유성구에서 현재 날씨와 날씨를 토대로 러닝하기 좋은 날씨인지 판별해줘.\n" +
//                "예시) 현재 날씨 \n" +
//                "온도, 습도, 강수확률, 강수량, 최고/최저 기온, 미세먼지\n" +
//                "추후 5시간 후까지 1시간 간격으로 온도 변화와 강수 확률 5개 예시 ");
        ChatResponse weatherResponse = alanService.getResonse(weatherRequest);

        Map<String, String> weatherInfo = new HashMap<>();
        weatherInfo.put("weather", weatherResponse.getContent());
        return weatherInfo;
    }

    @GetMapping("/chat/location")
    public Map<String, String> getLocationInfo(Model model) {
        String template = null;
        try {
            template = Files.readString(
                    new ClassPathResource("prompts/location.txt").getFile().toPath(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String userLocation = "대전광역시 유성구"; // 로그인 유저 위치
        String prompt = template.replace("{LOCATION}", userLocation);

        ChatRequest locationRequest = alanService.createRequest(prompt);
//        ChatRequest locationRequest = alanService.createRequest("대전광역시 유성구에서 사람들이 가장 많이 애용하는 러닝 코스를 알려줘. 결과물은 러닝 코스는 대전광역시 유성구 위치 기준으로 구단위 내에서 사람들이 많이 다니는 순서로 내림차순으로 리스트화 해줘.\n" +
//                "예시) 러닝 코스 장소 이름. \n" +
//                "1. 러닝 코스 길이\n" +
//                "2. 다녀간 사람들의 추천 수 \n" +
//                "3. 러닝 코스 한 줄 요약 소개 \n" +
//                "출처 정보는 제외");
        ChatResponse locationResponse = alanService.getResonse(locationRequest);

        Map<String, String> locationInfo = new HashMap<>();
        locationInfo.put("place", locationResponse.getContent());
        return locationInfo;
    }
}
