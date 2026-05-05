package com.example.scan_dineCustomer.Utility;

import com.example.scan_dineCustomer.enums.StatusCode;
import com.example.scan_dineCustomer.restaurant.dto.BaseResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;



public class BaseResponseUtil {

    public static BaseResponse createSuccessBaseResponse() {
        return new BaseResponse(
                StatusCode.OK.getErrorState(), StatusCode.OK.getStatusMessage(), StatusCode.OK.getCode());
    }

    public static BaseResponse createNoDataBaseResponse() {
        return new BaseResponse(
                StatusCode.NO_DATA.getErrorState(),
                StatusCode.NO_DATA.getStatusMessage(),
                StatusCode.NO_DATA.getCode());
    }

    public static BaseResponse createErrorBaseResponse() {
        return new BaseResponse(
                StatusCode.SERVER_ERROR.getErrorState(),
                StatusCode.SERVER_ERROR.getStatusMessage(),
                StatusCode.SERVER_ERROR.getCode());
    }

    public static BaseResponse createErrorBaseResponse(String errorMessage) {
        return new BaseResponse(
                StatusCode.SERVER_ERROR.getErrorState(), errorMessage, StatusCode.SERVER_ERROR.getCode());
    }

    public static BaseResponse createBaseResponse(@NotNull StatusCode code) {
        return new BaseResponse(code.getErrorState(), code.getStatusMessage(), code.getCode());
    }

    public static <T extends BaseResponse> T createBaseResponse(
            T response, @NotNull StatusCode code) {
        response.setEs(code.getErrorState());
        response.setMessage(code.getStatusMessage());
        response.setStatusCode(code.getCode());
        return response;
    }

    public static <T extends BaseResponse, T2 extends BaseResponse> T createBaseResponse(
            T response, @NotNull StatusCode code, T2 externalResponse) {
        response.setEs(code.getErrorState());
        if (externalResponse != null && StringUtils.isNotBlank(externalResponse.getMessage())) {
            response.setMessage(externalResponse.getMessage());
        } else {
            response.setMessage(code.getStatusMessage());
        }
        response.setStatusCode(code.getCode());
        return response;
    }

    public static <T extends BaseResponse> T createBaseResponse(
            T response, @NotNull StatusCode code, String message) {
        response.setEs(code.getErrorState());
        response.setMessage(message);
        response.setStatusCode(code.getCode());
        return response;
    }

}
