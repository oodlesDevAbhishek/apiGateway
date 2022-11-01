package com.oodles.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oodles.authService.AuthService;
import com.oodles.web.utils.Response;

@RestController
public class AuthenticationController {
	
	@Autowired AuthService authService;
	
	@PostMapping("/user/logout")
	public ResponseEntity<Object> userLogout(@RequestHeader("Authorization") String accessToken,
			@RequestParam(required = false) String browserId) {
		Object response = authService.invalidateUserToken(accessToken, browserId);
		if (Objects.nonNull(response)) {
			return Response.generateResponse(HttpStatus.OK, response, "SUCCESS" , true);
		}
		return Response.generateResponse(HttpStatus.EXPECTATION_FAILED, null, "FAILED", false);
	}
}
