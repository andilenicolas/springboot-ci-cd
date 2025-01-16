package com.example.project.interceptor;

import org.slf4j.MDC;
import java.util.UUID;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class HttpLoggingInterceptor extends CommonsRequestLoggingFilter implements HandlerInterceptor 
{
	private final String[] URIs_TO_EXCLUDE = { "/api-docs", "/swagger-ui" };
	private final ObjectMapper mapper = new ObjectMapper();
	
	 @Override
	 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException 
	 {
		 CachedBodyHttpServletRequest cachedBodyRequest = new CachedBodyHttpServletRequest(request);
		 ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		 String requestId = UUID.randomUUID().toString();
		 String  requestURI = request.getRequestURI();
		 
		 boolean shouldAddContextToLogs = !shouldExcludeURI(requestURI);
		 if(shouldAddContextToLogs) 
		 {
			 String requestBody = new String(cachedBodyRequest.getCachedBody(), request.getCharacterEncoding());
			 String userId = request.getHeader("X-User-ID");	

			 log.info("Request received. {RequestId}, {Method}, {URI}, {UserId}, {RequestBody}",
		               requestId, request.getMethod(), requestURI, userId, mapper.readValue(requestBody, Object.class)
		             );
		        
			 MDC.put("requestId", requestId);
			 MDC.put("userId", userId);
			 MDC.put("requestURI", requestURI);
			 MDC.put("requestMethod", request.getMethod());
			 MDC.put("requestBody", requestBody);   
		 }
		 
		 String responseBody = "";
		 try 
		 {
			 filterChain.doFilter(cachedBodyRequest, responseWrapper);
			 
			 byte[] responseArray = responseWrapper.getContentAsByteArray();
			 String characterEncoding = responseWrapper.getCharacterEncoding();
			 responseBody = new String(responseArray, characterEncoding != null ? Charset.forName(characterEncoding) : StandardCharsets.UTF_8);
			 
			 responseWrapper.copyBodyToResponse();
	     } finally {
	    	 MDC.clear();
	    	 
	    	 if (shouldAddContextToLogs) {
				 log.info("Request completed with {RequestId}, {URI}, {responseStatus}, {ResponseBody}",
						  requestId, requestURI, String.valueOf(responseWrapper.getStatus()), mapper.readValue(responseBody, Object.class)
						 );
	    	 }
	    }
	}
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }  
    
    private boolean shouldExcludeURI(String requestURI)
    {
    	for(String uri : URIs_TO_EXCLUDE)
    	{
    		if(requestURI.contains(uri)) {
    			return true;
    		}	
    	}
    	
    	return false;
    }
}
