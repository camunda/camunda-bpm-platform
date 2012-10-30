package com.camunda.fox.cycle.web.service.resource;

import java.util.List;

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

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.web.dto.UserDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;

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
    return UserDTO.wrap(userRepository.findById(id));
  }

  @POST
  @Path("{id}")
  @RolesAllowed("admin")
  @Transactional
  public UserDTO update(UserDTO data) {
    long id = data.getId();

    User user = userRepository.findById(id);
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
    User user = userRepository.findById(id);
    if (user == null) {
      throw notFound("User not found");
    }
    userRepository.delete(user);
  }

  // querying /////////////////////////////////////////////////////
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    return userRepository.isNameValid(name);
  }

  // update functionality //////////////////////////////////////////
  
  /**
   * Updates the user with the given data
   *
   * @param user
   * @param data
   */
  private void update(User user, UserDTO data) {
    user.setName(data.getName());
    user.setEmail(data.getEmail());
  }
}
