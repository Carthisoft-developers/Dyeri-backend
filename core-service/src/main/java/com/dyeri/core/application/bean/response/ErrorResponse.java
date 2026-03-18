package com.dyeri.core.application.bean.response;
import java.time.Instant;
import java.util.Map;
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path, String traceId, Map<String,String> fieldErrors) {}
