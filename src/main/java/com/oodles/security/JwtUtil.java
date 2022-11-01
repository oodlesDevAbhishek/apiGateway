package com.oodles.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.oodles.domains.TokenManagement;
import com.oodles.domains.TokenStatus;
import com.oodles.domains.User;
import com.oodles.repository.TokenManagementRepo;
import com.oodles.web.StringConstants;
import com.oodles.web.utils.SimpleIdGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {

	@Autowired TokenManagementRepo tokenManagementRepo;
	@Autowired SimpleIdGenerator idGenerator;
	
	@Value("${jwt.secret.key}")
	private String secretKey;
	
	private static final long HOUR_MILLIS = (1000L * 60L * 60L); 
	
	public String generateToken(String chatRoomId) {
		return Jwts.builder()
					.setSubject(chatRoomId)
					.signWith(SignatureAlgorithm.HS256, secretKey)
					.compact();
	}
	
	private Claims getAllClaims(String accessToken) {
		return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(accessToken).getBody();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCustomClaims(String token, String claim) {
		return (T) getAllClaims(token).get(claim);
	}
	
	public <T> T getClaimFromToken(String token , Function<Claims, T> claimResolver) {
		final Claims claims = getAllClaims(token);			
		return claimResolver.apply(claims);
	}
	
	public String generateAuthorizationToken(User user) {
		String tokenId =idGenerator.generateRandomId();
		Map<String, Object> claims = new HashMap<>();
		claims.put(StringConstants.ROLES, user.getRoles());
		claims.put(StringConstants.USERNAME, user.getUsername());
		claims.put(StringConstants.USER_ID, user.getUserId());
		storeUserTokenId(tokenId, user.getUserId());
		return Jwts.builder()
				.setSubject(user.getEmail())
				.addClaims(claims)
				.setId(tokenId)
				.signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
				.setExpiration(new Date(System.currentTimeMillis() + HOUR_MILLIS * 8))
				.compact();
	}
	
	private void storeUserTokenId(String tokenId, Long userId) {
		Optional<TokenManagement> managementStream = tokenManagementRepo.findByUserId(userId);
		if (managementStream.isPresent()) {
			TokenManagement tokenManagement = managementStream.get();
			List<TokenStatus> tokenStatus = tokenManagement.getTokenStatus();
			tokenStatus.add(new TokenStatus(tokenId, LocalDateTime.now(), true));
			tokenManagement.setTokenStatus(tokenStatus);
			TokenManagement saved = tokenManagementRepo.save(tokenManagement);
			new Thread(() -> removeTokenIdAfterTokenExpire(saved)).start();
		}else {
			TokenManagement tokenManagement = new TokenManagement();
			tokenManagement.setUserId(userId);
			List<TokenStatus> tokenStatus = new ArrayList<>();
			tokenStatus.add(new TokenStatus(tokenId, LocalDateTime.now(), true));
			tokenManagement.setTokenStatus(tokenStatus);
			tokenManagementRepo.save(tokenManagement);
		}
	}

	private void removeTokenIdAfterTokenExpire(TokenManagement saved) {
		List<TokenStatus> validTokenId = saved.getTokenStatus().stream().filter(tokenId-> tokenId.getIssueTime()
				.isAfter(LocalDateTime.now().minusHours(8))).collect(Collectors.toList());
		saved.setTokenStatus(validTokenId);
		tokenManagementRepo.save(saved);
	}

	private static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
	}
		
	public boolean isValidSignedToken(String token) {
		return Jwts.parser().setSigningKey(secretKey.getBytes()).isSigned(token);
	}
	
	public boolean isTokenExpired(String token) {
		Date expirationTime = getClaimFromToken(token, Claims::getExpiration);
		return expirationTime.before(new Date(System.currentTimeMillis()));
	}
	
	public boolean isTokenAllow(String token) {
		String tokenId = getCustomClaims(token, "jti");
		long userId = Long.parseLong(getCustomClaims(token, StringConstants.USER_ID).toString());
		Optional<TokenManagement> tokenStatusStream = tokenManagementRepo.findByUserId(userId);
		if (tokenStatusStream.isPresent()) {
			List<TokenStatus> tokenStatus = tokenStatusStream.get().getTokenStatus();
			Optional<TokenStatus> tokenStream = tokenStatus.stream().filter(tokenData-> tokenData.getTokenId().equals(tokenId)).findAny();
			if (tokenStream.isPresent()) {
				return tokenStream.get().isValid();
			}
		}
		return false;
	}
	
	public org.springframework.security.core.userdetails.User getUserFromJWT(String token) {
		String email = getCustomClaims(token, "sub");
		List<String> roles = getCustomClaims(token , StringConstants.ROLES);
		Set<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
					.collect(Collectors.toSet());
		return new org.springframework.security.core.userdetails.User(email, "", authorities);
	}
	
	public User getUserFromToken(String token) {
		String filteredToken = token.replace("Bearer", "");
		long userId = Long.parseLong(getCustomClaims(filteredToken, StringConstants.USER_ID).toString());
		String username = getCustomClaims(filteredToken, StringConstants.USERNAME);
		String email = getCustomClaims(filteredToken, "sub");
		List<String> roles = getCustomClaims(filteredToken , StringConstants.ROLES);
		return new User(userId, username, email, roles);
	}
}
