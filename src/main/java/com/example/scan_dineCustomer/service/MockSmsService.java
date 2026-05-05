package com.example.scan_dineCustomer.service;

import lombok.extern.slf4j.Slf4j;

// Not registered as @Service — TwilioSmsService is the active implementation.
// Re-add @Service and remove it from TwilioSmsService to switch back to mock (for local dev without Twilio).
@Slf4j
public class MockSmsService implements SmsService {

    @Override
    public void sendOtp(String mobile, String otp) {
        log.info("===== OTP for {} : {} =====", mobile, otp);
    }
}
