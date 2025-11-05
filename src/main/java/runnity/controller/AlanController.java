package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import runnity.dto.ChatRequest;
import runnity.dto.ChatResponse;
import runnity.service.AlanService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alan")
@RequiredArgsConstructor
public class AlanController {

    private final AlanService alanService;

    @GetMapping("/chat/weather")
    public Map<String, String> getWeatherInfo(Model model) {
        ChatRequest weatherRequest = alanService.createRequest("현재 대전의 날씨는 어떠한가요?");
        ChatResponse weatherResponse = alanService.getResonse(weatherRequest);

        Map<String, String> weatherInfo = new HashMap<>();
        weatherInfo.put("weather", weatherResponse.getContent());
        return weatherInfo;
    }

    @GetMapping("/chat/location")
    public Map<String, String> getLocationInfo(Model model) {
        ChatRequest locationRequest = alanService.createRequest("현재 대전에서 러닝하기 좋은 장소는 어디인가요? 장소 명칭과 간단한 1줄 설명으로 요약해주세요. 출처 정보는 제외해주세요");
        ChatResponse locationResponse = alanService.getResonse(locationRequest);

        Map<String, String> locationInfo = new HashMap<>();
        locationInfo.put("place", locationResponse.getContent());
        return locationInfo;
    }
}
