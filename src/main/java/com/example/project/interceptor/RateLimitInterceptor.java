package com.example.project.interceptor;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.project.config.RateLimitConfig;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = getApiKey(request);
        
        if (apiKey == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Missing API key\"}");
            return false;
        }

        Bucket bucket = rateLimitConfig.resolveBucket(apiKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.getWriter().write(String.format(
            "{\"error\": \"Rate limit exceeded. Please wait %d seconds\"}",
            waitForRefill
        ));
        return false;
    }

    private String getApiKey(HttpServletRequest request) {
    	return "";
    	
        // First try to get from header
        //String apiKey = request.getHeader("X-API-Key");
        //if (apiKey != null) {
          //  return apiKey;
        //}

        // Then try to get from query parameter
        //return request.getParameter("apiKey");
    }
}