package com.oodles.authService.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oodles.authService.AuthService;
import com.oodles.domains.TokenManagement;
import com.oodles.domains.TokenStatus;
import com.oodles.repository.TokenManagementRepo;
import com.oodles.security.JwtUtil;


@Service
public class AuthServiceImpl implements AuthService{
	
	@Autowired TokenManagementRepo tokenManagementRepo;
	@Autowired private JwtUtil jwtUtil;
	
	
	@Override
	public Object invalidateUserToken(String accessToken, String deviceId) {
		String token = accessToken.replace("Bearer", "");
		String tokenId = jwtUtil.getCustomClaims(token, "jti");
		long userId = Long.parseLong(jwtUtil.getCustomClaims(token, "userId").toString());
		Optional<TokenManagement> tokenStatusStream = tokenManagementRepo.findByUserId(userId);
		if (tokenStatusStream.isPresent()) {
			TokenManagement tokenManagement = tokenStatusStream.get();
			List<TokenStatus> tokenStatus = tokenManagement.getTokenStatus();
			Optional<TokenStatus> tokenStream = tokenStatus.stream().filter(tokenData-> tokenData.getTokenId().equals(tokenId)).findAny();
			if (tokenStream.isPresent()) {
				TokenStatus changeStatus = tokenStream.get();
				int indexOfObject = tokenStatus.indexOf(changeStatus);
				changeStatus.setValid(false);
				tokenStatus.set(indexOfObject, changeStatus);
				tokenManagement.setTokenStatus(tokenStatus);
				tokenManagementRepo.save(tokenManagement);
				//write metthod to brodcast in the dwonstream microservies
				//broadcastLogoutEvent(userId, browserId);
				return true;
			}
		}
		return false;
	}

}
