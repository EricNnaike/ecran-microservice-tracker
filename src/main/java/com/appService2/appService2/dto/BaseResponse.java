package com.appService2.appService2.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class BaseResponse<T> {

    private HttpStatus status;
    private String message;
    private T result;

    public BaseResponse() {
    }

    public BaseResponse(HttpStatus status, String message, T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

}
