package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Registration request entity.
 * 
 * @author kzq
 */
@Entity
@Table(name = "T_REGISTRATION_REQUEST")
public class RegistrationRequest implements Loggable {

    @Override
    public Date getDeleteDate() {
        return null; // Replace with actual logic if needed
    }
    /**
     * Registration request ID.
     */
    @Id
    @Column(name = "RGR_ID_C", length = 36)
    private String id;
    
    /**
     * Username.
     */
    @Column(name = "RGR_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * Email.
     */
    @Column(name = "RGR_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Hashed password.
     */
    @Column(name = "RGR_PASSWORD_C", nullable = false, length = 100)
    private String password;
    
    /**
     * Creation date.
     */
    @Column(name = "RGR_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Status (PENDING, APPROVED, REJECTED).
     */
    @Column(name = "RGR_STATUS_C", nullable = false, length = 10)
    private String status;

    /**
     * IP address.
     */
    @Column(name = "RGR_IPADDRESS_C", length = 45)
    private String ipAddress;
    
    /**
     * Date of status change.
     */
    @Column(name = "RGR_STATUSDATE_D")
    private Date statusDate;
    
    /**
     * User ID who changed the status.
     */
    @Column(name = "RGR_STATUSUSERID_C", length = 36)
    private String statusUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public RegistrationRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RegistrationRequest setEmail(String email) {
        this.email = email;
        return this;
    }
    
    public String getPassword() {
        return password;
    }

    public RegistrationRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public RegistrationRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public RegistrationRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public RegistrationRequest setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public RegistrationRequest setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
        return this;
    }

    public String getStatusUserId() {
        return statusUserId;
    }

    public RegistrationRequest setStatusUserId(String statusUserId) {
        this.statusUserId = statusUserId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }
}