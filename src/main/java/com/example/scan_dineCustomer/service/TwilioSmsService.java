package com.example.scan_dineCustomer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSmsService implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    private final RestTemplate restTemplate;

    @Override
    public void sendOtp(String mobile, String otp) {
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        // Twilio requires E.164 format (+91XXXXXXXXXX for India)
        String toNumber = mobile.startsWith("+") ? mobile : "+91" + mobile;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(accountSid, authToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("From", fromNumber);
        body.add("To", toNumber);
        body.add("Body", "Your Scan & Dine OTP is: " + otp + ". Valid for 5 minutes. Do not share with anyone.");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("OTP sent successfully to {}", mobile);
        } catch (Exception e) {
            log.error("Twilio SMS failed for {}: {}", mobile, e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }
}
