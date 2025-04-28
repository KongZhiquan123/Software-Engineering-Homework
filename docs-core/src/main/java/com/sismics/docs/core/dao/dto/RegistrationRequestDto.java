package com.sismics.docs.core.dao.dto;

/**
 * Registration request DTO.
 *
 * @author YourName
 */
public class RegistrationRequestDto {
    /**
     * Registration request ID.
     */
    private String id;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Email.
     */
    private String email;
    
    /**
     * Creation timestamp.
     */
    private Long createTimestamp;
    
    /**
     * Status.
     */
    private String status;
    
    /**
     * IP address.
     */
    private String ipAddress;
    
    /**
     * Status change timestamp.
     */
    private Long statusTimestamp;
    
    /**
     * Status user ID.
     */
    private String statusUserId;
    
    /**
     * Status username.
     */
    private String statusUsername;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(Long statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    public String getStatusUserId() {
        return statusUserId;
    }

    public void setStatusUserId(String statusUserId) {
        this.statusUserId = statusUserId;
    }

    public String getStatusUsername() {
        return statusUsername;
    }

    public void setStatusUsername(String statusUsername) {
        this.statusUsername = statusUsername;
    }
}