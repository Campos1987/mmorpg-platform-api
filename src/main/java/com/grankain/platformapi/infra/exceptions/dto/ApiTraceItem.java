package com.grankain.platformapi.infra.exceptions.dto;

import java.time.Instant;
import java.util.List;

/**
 * Representa um único frame (linha) de uma exceção no Java.
 * Usado para facilitar a depuração no frontend/ambiente de DEV.
 */
public record ApiTraceItem(
        String className,
        String fileName,
        int lineNumber,
        String methodName
) {
}

