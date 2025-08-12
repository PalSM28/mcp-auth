package com.ninja.auth_service.dto;


//import javax.validation.constraints.NotBlank;

public class AuthenticationRequest {
    //@NotBlank(message = "API key is required")
    private String apiKey;
    
    public AuthenticationRequest() {}
    
    public AuthenticationRequest(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}