package com.sismics.docs.core.dao;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.dto.RegistrationRequestDto;
import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.*;
import java.sql.Timestamp;

/**
 * Registration request DAO.
 * 
 * @author kzq
 */
public class RegistrationRequestDao {
    /**
     * Creates a new registration request.
     * 
     * @param registrationRequest Registration request to create
     * @return ID of the created registration request
     */
    public String create(RegistrationRequest registrationRequest) {
        // Create the UUID
        registrationRequest.setId(UUID.randomUUID().toString());
        
        // Create the registration request
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        registrationRequest.setCreateDate(new Date());
        registrationRequest.setStatus("PENDING");
        em.persist(registrationRequest);
        
        return registrationRequest.getId();
    }
    
    /**
     * Returns an active registration request by ID.
     * 
     * @param id ID
     * @return Registration request
     */
    public RegistrationRequest getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from RegistrationRequest r where r.id = :id");
            q.setParameter("id", id);
            return (RegistrationRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns an active registration request by username.
     * 
     * @param username Username
     * @return Registration request
     */
    public RegistrationRequest getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from RegistrationRequest r where r.username = :username and r.status = 'PENDING'");
            q.setParameter("username", username);
            return (RegistrationRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Updates a registration request.
     * 
     * @param registrationRequest Registration request to update
     * @return Updated registration request
     */
    public RegistrationRequest update(RegistrationRequest registrationRequest) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the registration request
        Query q = em.createQuery("select r from RegistrationRequest r where r.id = :id");
        q.setParameter("id", registrationRequest.getId());
        RegistrationRequest registrationRequestDb = (RegistrationRequest) q.getSingleResult();

        // Update the registration request
        registrationRequestDb.setStatus(registrationRequest.getStatus());
        registrationRequestDb.setStatusDate(registrationRequest.getStatusDate());
        registrationRequestDb.setStatusUserId(registrationRequest.getStatusUserId());
        
        return registrationRequestDb;
    }
    
    /**
     * Returns the list of all registration requests.
     * 
     * @param sortCriteria Sort criteria
     * @return List of registration requests
     */
    public List<RegistrationRequestDto> findAll(SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select r.RGR_ID_C as c0, r.RGR_USERNAME_C as c1, r.RGR_EMAIL_C as c2, r.RGR_CREATEDATE_D as c3, r.RGR_STATUS_C as c4, r.RGR_IPADDRESS_C as c5, r.RGR_STATUSDATE_D as c6, r.RGR_STATUSUSERID_C as c7, u.USE_USERNAME_C as c8 ");
        sb.append(" from T_REGISTRATION_REQUEST r ");
        sb.append(" left join T_USER u on u.USE_ID_C = r.RGR_STATUSUSERID_C ");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<RegistrationRequestDto> registrationRequestDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
            registrationRequestDto.setId((String) o[i++]);
            registrationRequestDto.setUsername((String) o[i++]);
            registrationRequestDto.setEmail((String) o[i++]);
            registrationRequestDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            registrationRequestDto.setStatus((String) o[i++]);
            registrationRequestDto.setIpAddress((String) o[i++]);
            Timestamp statusTimestamp = (Timestamp) o[i++];
            if (statusTimestamp != null) {
                registrationRequestDto.setStatusTimestamp(statusTimestamp.getTime());
            }
            registrationRequestDto.setStatusUserId((String) o[i++]);
            registrationRequestDto.setStatusUsername((String) o[i++]);
            registrationRequestDtoList.add(registrationRequestDto);
        }
        
        return registrationRequestDtoList;
    }
}