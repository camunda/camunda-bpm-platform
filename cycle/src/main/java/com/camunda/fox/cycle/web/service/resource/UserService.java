package com.camunda.fox.cycle.web.service.resource;

import java.security.Principal;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.security.IdentityHolder;
import com.camunda.fox.cycle.web.dto.UserDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;
import com.camunda.fox.security.UserIdentity;

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

  @Inject
  private UserRepository userRepository;

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
  public String changePassword(
      @PathParam("id") long userId, 
      @FormParam("oldPassword") String oldPassword, 
      @FormParam("newPassword") String newPassword) {
    
    UserIdentity principal = IdentityHolder.getIdentity();
    User user = getUserById(userId);
    
    if (principal != null && principal.getName().equals(user.getName())) {
      
      // TODO: decrypt password
      if (oldPassword.equals(user.getPassword())) {
        // TODO: encrypt password
        user.setPassword(newPassword);
        
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
    
    // TODO: encrypt password
    if (data.getPassword() != null) {
      user.setPassword(data.getPassword());
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
