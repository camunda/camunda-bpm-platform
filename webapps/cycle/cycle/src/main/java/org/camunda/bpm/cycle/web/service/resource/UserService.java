package org.camunda.bpm.cycle.web.service.resource;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.connector.crypt.EncryptionService;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.repository.UserRepository;
import org.camunda.bpm.cycle.security.IdentityHolder;
import org.camunda.bpm.cycle.service.mail.MailService;
import org.camunda.bpm.cycle.service.mail.MailServiceException;
import org.camunda.bpm.cycle.web.dto.PasswordChangeDTO;
import org.camunda.bpm.cycle.web.dto.UserDTO;
import org.camunda.bpm.cycle.web.service.AbstractRestService;
import org.camunda.bpm.security.UserIdentity;
import org.springframework.transaction.annotation.Transactional;


/**
 * This is the user rest controller which exposes user
 * <code>list</code>,
 * <code>get</code>,
 * <code>create</code> and<code>update</code> methods as well as some utilities to the cycle client application.
 *
 * The arrangement of methods is compatible with angular JS
 * <code>$resource</code>.
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/user")
public class UserService extends AbstractRestService {
  
  private Logger log = Logger.getLogger(UserService.class.getName());

  @Inject
  private UserRepository userRepository;
  @Inject
  private MailService mailService;
  @Inject
  private CycleConfiguration configuration;
  @Inject
  private EncryptionService encryptionService;

  /**
   * $resource specific methods
   */
  @GET
  public List<UserDTO> list() {
    return UserDTO.wrapAll(userRepository.findAll());
  }

  @GET
  @Path("{id}")
  public UserDTO get(@PathParam("id") long id) {
    return UserDTO.wrap(getUserById(id));
  }

  @POST
  @Path("{id}")
  @RolesAllowed("admin")
  @Transactional
  public UserDTO update(UserDTO data) {
    long id = data.getId();

    User user = getUserById(id);
    if (user == null) {
      throw notFound("user not found");
    }

    update(user, data);
    return UserDTO.wrap(user);
  }

  @POST
  @RolesAllowed("admin")
  public UserDTO create(UserDTO data) {
    User user = new User();
    update(user, data);
    
    String sendEmailResult = sendWelcomeEmail(user, data.getPassword());
    // TODO: add email operation status result to response
    
    return UserDTO.wrap(userRepository.saveAndFlush(user));
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("admin")
  @Transactional
  public void delete(@PathParam("id") long id) {
    User user = getUserById(id);
    if (user == null) {
      throw notFound("User not found");
    }
    userRepository.delete(user);
  }

  // querying /////////////////////////////////////////////////////

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameAvailable")
  public boolean isNameAvailable(@QueryParam("name") String name) {
    return userRepository.isNameAvailable(name);
  }

  // update functionality //////////////////////////////////////////
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}/changePassword")
  @Transactional
  public String changePassword(@PathParam("id") long userId, PasswordChangeDTO data) {
    
    UserIdentity principal = IdentityHolder.getIdentity();
    User user = getUserById(userId);
    
    if (principal != null && principal.getName().equals(user.getName())) {
      if (encryptionService.checkUserPassword(data.getOldPassword(), user.getPassword())) {
        user.setPassword(encryptionService.encryptUserPassword(data.getNewPassword()));
        return "Ok";
      }
    }
    
    throw notAllowed("invalid credentials");
  }

  /**
   * Updates the user with the given data
   *
   * @param user
   * @param data
   */
  private void update(User user, UserDTO data) {
    user.setName(data.getName());
    user.setEmail(data.getEmail());
    
    user.setAdmin(data.isAdmin());
    
    if (data.getPassword() != null) {
      user.setPassword(encryptionService.encryptUserPassword(data.getPassword()));
    }
  }
  
  /**
   * Sends a welcome email to the user. If the email can be sent successfully,
   * this method returns the string "success". Otherwise this method returns an
   * exception message.
   * 
   * @param user
   *          the new user
   * @return a string indicating the outcome of the email sending operation
   */
  protected String sendWelcomeEmail(User user, String password) {
    
    String emailFrom = configuration.getEmailFrom();
    
    String email = user.getEmail();
    
    try {
      
      mailService.sendWelcomeEmail(email, password, emailFrom, email);
      return "success";
      
    } catch (MailServiceException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return e.getMessage();
      
    } catch (Exception e) {
      log.log(Level.SEVERE, "An unexpected exception occured while trying to send an email", e);
      return "An unexpected exception occured while trying to send an email: "+e.getMessage();
      
    }
    
    
  }

  // internal accessing ///////////////////////////////////////////
  
  protected User getUserById(long id) {
    User user = userRepository.findById(id);
    if (user == null) {
      throw notFound("user not found");
    }
    
    return user;
  }
}
