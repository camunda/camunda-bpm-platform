package org.camunda.bpm.cycle.web.service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.repository.UserRepository;
import org.camunda.bpm.cycle.security.IdentityHolder;
import org.camunda.bpm.cycle.web.dto.UserDTO;
import org.camunda.bpm.cycle.web.service.resource.UserService;
import org.camunda.bpm.security.UserIdentity;


/**
 *
 * @author nico.rehwaldt
 */
@Path("first-time-setup")
public class InitialConfigurationService extends AbstractRestService {
  
  private static UserIdentity TEMP_AUTHORIZED_IDENTITY = new UserIdentity(new User("temp-user", true));
  
  @Inject
  private CycleConfiguration configuration;
  
  @Inject
  private UserService userService;
  
  @Inject
  private UserRepository userRepository;
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Object createInitialUser() {
    if (isConfigured()) {
      return redirectTo("secured/view/index");
    }
    
    return "tpl:app/first-time-setup";
  }

  @POST
  public String createInitialUser(UserDTO data) {
    if (isConfigured()) {
      throw notAllowed("already configured");
    }
    
    if (data.getEmail() == null || data.getPassword() == null || data.getName() == null) {
      throw badRequest("invalid fields");
    }
    
    data.setAdmin(true);
    
    UserDTO user = null;
    
    // temporary overload to allow user creation
    try {
      IdentityHolder.setIdentity(TEMP_AUTHORIZED_IDENTITY);
      user = userService.create(data);
    } finally {
      IdentityHolder.setIdentity(null);
    }
    
    return "Ok [id=" + user.getId() + "]";
  }

  private boolean isConfigured() {
    return configuration.isUseJaas() || (userRepository.countAll() > 0);
  }
}
