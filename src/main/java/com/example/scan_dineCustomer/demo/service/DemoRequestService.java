package com.example.scan_dineCustomer.demo.service;

import com.example.scan_dineCustomer.demo.dto.DemoRequestPayload;
import com.example.scan_dineCustomer.demo.dto.DemoRequestResponse;
import com.example.scan_dineCustomer.demo.entity.DemoRequest;
import com.example.scan_dineCustomer.demo.repository.DemoRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DemoRequestService {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10,15}$");

    private final DemoRequestRepository demoRequestRepository;

    @Transactional
    public DemoRequestResponse create(DemoRequestPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (!StringUtils.hasText(payload.getName())) {
            throw new IllegalArgumentException("name is required");
        }
        if (!StringUtils.hasText(payload.getMobile())) {
            throw new IllegalArgumentException("mobile is required");
        }
        if (!MOBILE_PATTERN.matcher(payload.getMobile().trim()).matches()) {
            throw new IllegalArgumentException("mobile must contain 10 to 15 digits");
        }

        DemoRequest request = new DemoRequest();
        request.setName(payload.getName().trim());
        request.setMobile(payload.getMobile().trim());
        request.setEmail(StringUtils.hasText(payload.getEmail()) ? payload.getEmail().trim() : null);

        return DemoRequestResponse.from(demoRequestRepository.save(request));
    }
}
