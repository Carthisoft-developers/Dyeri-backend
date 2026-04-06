package com.dyeri.core.application.bean.response;
import java.time.Instant;
public record TimelineStepResponse(String status, String label, Instant timestamp) {}
