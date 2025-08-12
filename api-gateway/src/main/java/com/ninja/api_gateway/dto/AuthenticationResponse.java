package com.ninja.api_gateway.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    @JsonProperty("expires_in")
    private Long expiresIn;
    private boolean success;
   
	private String message;
    @JsonProperty("client_name")
    private String clientName;
    
    private List<String> scopes;
    
    
    
    public AuthenticationResponse(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.success = true;
        this.message = "Authentication successful";
    }
    
    public AuthenticationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
    public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}
}


