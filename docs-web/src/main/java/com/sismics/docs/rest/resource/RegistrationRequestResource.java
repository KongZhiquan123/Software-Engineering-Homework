package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.RegistrationRequestDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.dto.RegistrationRequestDto;
import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.jpa.SortCriteria;
// Removed incorrect import as it cannot be resolved
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil; // Ensure this is the correct ValidationUtil class

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Registration request REST resources.
 * 
 * @author kzq
 */
@Path("/registrationrequest")
public class RegistrationRequestResource extends BaseResource {
    /**
     * Creates a new registration request.
     *
     * @api {put} /registrationrequest Create a registration request
     * @apiName PutRegistrationRequest
     * @apiGroup RegistrationRequest
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) AlreadyExistingUsername Username already exists
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @param password Password
     * @param email E-mail
     * @return Response
     */
    @PUT
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");
        
        // Check if the username already exists in the user database
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user != null) {
            throw new ClientException("AlreadyExistingUsername", "Username already exists");
        }
        
        // Check if a registration request with this username is already pending
        RegistrationRequestDao registrationRequestDao = new RegistrationRequestDao();
        RegistrationRequest existingRequest = registrationRequestDao.getActiveByUsername(username);
        if (existingRequest != null) {
            throw new ClientException("AlreadyExistingUsername", "A request for this username is already pending");
        }
        
        // Create the registration request
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername(username);
        registrationRequest.setPassword(password);
        registrationRequest.setEmail(email);
        
        // Get the remote IP
        String ip = request.getHeader("x-forwarded-for");
        if (Strings.isNullOrEmpty(ip)) {
            ip = request.getRemoteAddr();
        }
        registrationRequest.setIpAddress(ip);
        
        // Create the request
        try {
            registrationRequestDao.create(registrationRequest);
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Unknown server error", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all registration requests.
     *
     * @api {get} /registrationrequest Get registration requests
     * @apiName GetRegistrationRequest
     * @apiGroup RegistrationRequest
     * @apiSuccess {Object[]} requests List of registration requests
     * @apiSuccess {String} requests.id ID
     * @apiSuccess {String} requests.username Username
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {Number} requests.create_date Create date (timestamp)
     * @apiSuccess {String} requests.status Status (PENDING, APPROVED, REJECTED)
     * @apiSuccess {String} requests.ip_address IP address
     * @apiSuccess {Number} [requests.status_date] Status change date (timestamp)
     * @apiSuccess {String} [requests.status_username] Username who changed the status
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Build the sorting criteria
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        
        // Get all registration requests
        RegistrationRequestDao registrationRequestDao = new RegistrationRequestDao();
        List<RegistrationRequestDto> registrationRequestDtoList = registrationRequestDao.findAll(sortCriteria);
        
        // Build the response
        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (RegistrationRequestDto registrationRequestDto : registrationRequestDtoList) {
            JsonObjectBuilder request = Json.createObjectBuilder()
                    .add("id", registrationRequestDto.getId())
                    .add("username", registrationRequestDto.getUsername())
                    .add("email", registrationRequestDto.getEmail())
                    .add("create_date", registrationRequestDto.getCreateTimestamp())
                    .add("status", registrationRequestDto.getStatus())
                    .add("ip_address", registrationRequestDto.getIpAddress());
            
            if (registrationRequestDto.getStatusTimestamp() != null) {
                request.add("status_date", registrationRequestDto.getStatusTimestamp());
            }
            
            if (registrationRequestDto.getStatusUsername() != null) {
                request.add("status_username", registrationRequestDto.getStatusUsername());
            }
            
            requests.add(request);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Process a registration request.
     *
     * @api {post} /registrationrequest/:id/approve Approve a registration request
     * @apiName PostRegistrationRequestApprove
     * @apiGroup RegistrationRequest
     * @apiParam {String} id Registration request ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Registration request not found
     * @apiError (client) AlreadyProcessed Registration request already processed
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id ID
     * @return Response
     */
    @POST
    @Path("{id: [a-zA-Z0-9-]+}/approve")
    public Response approve(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the registration request
        RegistrationRequestDao registrationRequestDao = new RegistrationRequestDao();
        RegistrationRequest registrationRequest = registrationRequestDao.getActiveById(id);
        if (registrationRequest == null) {
            throw new NotFoundException();
        }
        
        // Check if the registration request is already processed
        if (!"PENDING".equals(registrationRequest.getStatus())) {
            throw new ClientException("AlreadyProcessed", "Registration request already processed");
        }
        
        // Update the registration request
        registrationRequest.setStatus("APPROVED");
        registrationRequest.setStatusDate(new Date());
        registrationRequest.setStatusUserId(principal.getId());
        registrationRequestDao.update(registrationRequest);
        
        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setEmail(registrationRequest.getEmail());
        user.setStorageQuota(Constants.DEFAULT_USER_QUOTA);
        user.setOnboarding(true);
        
        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId());
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Unknown server error", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Reject a registration request.
     *
     * @api {post} /registrationrequest/:id/reject Reject a registration request
     * @apiName PostRegistrationRequestReject
     * @apiGroup RegistrationRequest
     * @apiParam {String} id Registration request ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Registration request not found
     * @apiError (client) AlreadyProcessed Registration request already processed
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id ID
     * @return Response
     */
    @POST
    @Path("{id: [a-zA-Z0-9-]+}/reject")
    public Response reject(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the registration request
        RegistrationRequestDao registrationRequestDao = new RegistrationRequestDao();
        RegistrationRequest registrationRequest = registrationRequestDao.getActiveById(id);
        if (registrationRequest == null) {
            throw new NotFoundException();
        }
        
        // Check if the registration request is already processed
        if (!"PENDING".equals(registrationRequest.getStatus())) {
            throw new ClientException("AlreadyProcessed", "Registration request already processed");
        }
        
        // Update the registration request
        registrationRequest.setStatus("REJECTED");
        registrationRequest.setStatusDate(new Date());
        registrationRequest.setStatusUserId(principal.getId());
        registrationRequestDao.update(registrationRequest);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}