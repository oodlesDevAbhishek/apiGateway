

package com.oodles.authService;

import org.springframework.stereotype.Service;

@Service
public interface AuthService {
	
	public Object invalidateUserToken(String accessToken, String browserId);
}
