package com.oodles.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class PerRequestFilter extends OncePerRequestFilter {

	@Autowired JwtUtil jwtUtil;
	ExceptionHandlerExceptionResolver handlerExceptionResolver;
	
	public PerRequestFilter(JwtUtil jwtUtil, ExceptionHandlerExceptionResolver handlerExceptionResolver) {
		this.jwtUtil = jwtUtil;
		this.handlerExceptionResolver=handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		try {
			String accessToken = request.getHeader("Authorization");
			if (Objects.nonNull(accessToken) && !accessToken.equals("") && accessToken.startsWith("Bearer")) {
				String filteredToken = accessToken.replace("Bearer", "");
				if (jwtUtil.isValidSignedToken(filteredToken) && !jwtUtil.isTokenExpired(filteredToken) && jwtUtil.isTokenAllow(filteredToken)) {
					User details = jwtUtil.getUserFromJWT(filteredToken);
					SecurityContextHolder.getContext().setAuthentication(getAuthentication(details));
				}
				filterChain.doFilter(request, response);
			} else {
				filterChain.doFilter(request, response);
			}
		} catch (MalformedJwtException | SignatureException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			handlerExceptionResolver.resolveException(request, response, null, e);
		}
	}

	private Authentication getAuthentication(User details) {
		return new UsernamePasswordAuthenticationToken(details, "", details.getAuthorities());
	}

}
