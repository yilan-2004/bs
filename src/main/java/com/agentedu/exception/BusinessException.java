package com.agentedu.exception;

import com.agentedu.common.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = Constants.FAIL_CODE;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
