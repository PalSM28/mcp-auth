package com.ninja.auth_service.dto;

public class AuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private boolean success;
    private String message;    
    private String clientName;
    
    public AuthenticationResponse(String accessToken, Long expiresIn, String clientName) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.success = true;
        this.message = "Authentication successful";
        this.clientName= clientName;
    }
    
    public AuthenticationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	// Getters and setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}


