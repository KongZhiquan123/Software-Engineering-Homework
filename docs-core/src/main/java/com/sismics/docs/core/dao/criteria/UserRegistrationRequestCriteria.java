package com.sismics.docs.core.dao.criteria;

/**
 * User registration request criteria.
 *
 * @author You
 */
public class UserRegistrationRequestCriteria {
    /**
     * Status (PENDING, APPROVED, REJECTED).
     */
    private String status;
    
    /**
     * Constructor.
     */
    public UserRegistrationRequestCriteria() {
    }

    public String getStatus() {
        return status;
    }

    public UserRegistrationRequestCriteria setStatus(String status) {
        this.status = status;
        return this;
    }
}