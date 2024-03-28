package com.example.springjwt.controller;

import com.example.springjwt.dto.KakaoReadyResponse;
import com.example.springjwt.service.KakaoPayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/kakaopay")
    public ResponseEntity<Object> forwardRequest(@RequestBody Object requestBody) {
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> getRequestData = objectMapper.convertValue(requestBody, Map.class);

        // Create HttpHeaders and set Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", getRequestData.get("authorization"));

        // Create HttpEntity with headers and request body
        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, headers);

        // Send request using RestTemplate.exchange method to include HttpHeaders
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);

        return responseEntity;
    }

    @PostMapping("/kakaopayAppr")
    public ResponseEntity<Object> forwardRequestAppr(@RequestBody Object requestBody) {
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> getRequestData = objectMapper.convertValue(requestBody, Map.class);
        System.out.println(getRequestData);     // 새 창에서 atom이 사라짐

        // Create HttpHeaders and set Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", getRequestData.get("authorization"));

        // Create HttpEntity with headers and request body
        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, headers);

        // Send request using RestTemplate.exchange method to include HttpHeaders
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);

            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 결제요청
     */
    @PostMapping("/ready")
    public KakaoReadyResponse readyToKakaoPay() {

        return kakaoPayService.kakaoPayReady();
    }
}
