package com.oodles.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oodles.domains.User;
import com.oodles.service.impl.UserServiceImpl;

public class ScaffoldAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final UserServiceImpl userService;
	private final JwtUtil jwtUtil;
	private final CryptionService cryptionService;

	private static final Logger log = LoggerFactory.getLogger(ScaffoldAuthenticationFilter.class);

	public ScaffoldAuthenticationFilter( UserServiceImpl userService,
			JwtUtil jwtUtil, CryptionService cryptionService) {
		super.setFilterProcessesUrl("/chat/auth/authorize");
		this.userService = userService;
		this.jwtUtil = jwtUtil;
		this.cryptionService = cryptionService;
	}
	
	@Autowired
	private AuthenticationManager authenticationManager;
	

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName());
		String username = obtainUsername(request);
		String password = obtainPassword(request);
		String decodedPassword = cryptionService.decrypt(password, System.getenv("PASSWORD_SECRET_KEY"));
		System.out.println("   pwd =======>"+decodedPassword);
		if (Objects.isNull(decodedPassword)) {
			decodedPassword = password;
		}
		if (Objects.isNull(username) || Objects.isNull(password)) {
			log.error("Unable to perform authentication as credentials are null.");
			return null;
		}
		return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, decodedPassword));
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		return request.getParameter("password").trim();
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		return request.getParameter("username").trim();
	}

	private String obtainDeviceId(HttpServletRequest request) {
		return request.getParameter("deviceId");
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		String deviceId = obtainDeviceId(request);
		String username = ((org.springframework.security.core.userdetails.User) authResult.getPrincipal())
				.getUsername();
		User user = userService.loadUserByEmail(username);
		String jwtToken = jwtUtil.generateAuthorizationToken(user);
		Map<String, Object> res = new HashMap<>();
		res.put("access_token", jwtToken);
		// res.put("isFcmToken", checkAndActivateFcmToken(user.getUserId(),
		// user.getEmail(), deviceId));
		res.put("val_hrs", 1000 * 60 * 60 * 8);
		response.setContentType("application/json");
		new ObjectMapper().writeValue(response.getWriter(), res);
	}

//	private boolean checkAndActivateFcmToken(Long userId, String email, String deviceId) {
//		return userService.checkAndActivateFcmToken(userId, email, deviceId);
//	}
}
