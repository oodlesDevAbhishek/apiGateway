package com.oodles.web.utils;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Response {
	private Response() {
	}

	public static final <T> ResponseEntity<Object> generateResponse(HttpStatus status, T payload, String message,
			boolean isSuccess) {
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("data", payload);
		responseData.put("message", message);
		responseData.put("isSuccess", isSuccess);
		responseData.put("timestamp", System.currentTimeMillis());
		return new ResponseEntity<>(responseData, status);
	}

}
