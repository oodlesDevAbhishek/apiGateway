package com.oodles.domains.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionRepo {
	
	private static UserSessionRepo instance;
    
    private UserSessionRepo() {        
    }
    
    public static UserSessionRepo getInstance() {
        if(instance == null) {
        	instance = new UserSessionRepo();
        }
        
        return instance;
    }

	private Map<String, UserEvent> activeSessions = new ConcurrentHashMap<>();

	public void add(String sessionId, UserEvent event) {
		activeSessions.put(sessionId, event);
	}

	public UserEvent getParticipant(String sessionId) {
		return activeSessions.get(sessionId);
	}

	public void removeParticipant(String sessionId) {
		activeSessions.remove(sessionId);
	}

	public Map<String, UserEvent> getActiveSessions() {
		return activeSessions;
	}

	public void setActiveSessions(Map<String, UserEvent> activeSessions) {
		this.activeSessions = activeSessions;
	}
}