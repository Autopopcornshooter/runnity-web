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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/alan")
@RequiredArgsConstructor
public class AlanController {

    private final AlanService alanService;

    @GetMapping("/chat/weather")
    public Map<String, Object> getWeatherInfo(Model model) {

        String prompt = alanService.getWeatherPrompt();
        ChatRequest weatherRequest = alanService.createRequest(prompt);
        ChatResponse weatherResponse = alanService.getResonse(weatherRequest);

        Map<String, Object> weatherData = alanService.weatherNomaliztion(weatherResponse);
        return weatherData; // Spring이 자동으로 JSON 변환
    }

    @GetMapping("/chat/location")
    public List<Map<String, Object>> getLocationInfo(Model model) {
        String prompt = alanService.getLocationPrompt();
        ChatRequest locationRequest = alanService.createRequest(prompt);
        ChatResponse locationResponse = alanService.getResonse(locationRequest);

        List<Map<String, Object>> locationData = alanService.locationNomaliztion(locationResponse);
//        Map<String, String> locationInfo = new HashMap<>();
//        locationInfo.put("place", locationResponse.getContent());
        return locationData;
    }
}
