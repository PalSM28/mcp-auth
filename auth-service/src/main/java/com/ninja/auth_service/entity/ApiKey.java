package com.ninja.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "api_keys")
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key", unique = true, nullable = false)
    private String key;
    
    @Column(name = "client_name", unique = true, nullable = false)
    private String clientName;
       
   /* @Column(name = "scopes")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Enumerated(EnumType.STRING)
    private Set<Scope> scopes;*/
    
    @Column(name = "active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
  
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();  
    }

    // Constructors
    public ApiKey() {}

    public ApiKey(String key, String clientName, LocalDateTime expiresAt, LocalDateTime createdAt, boolean isActive) {
    	this.key = key;
        this.clientName = clientName;        
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientId(String clientName) { this.clientName = clientName; }

    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
    
    
}
