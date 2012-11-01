package com.camunda.fox.cycle.web.service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.camunda.fox.cycle.configuration.CycleConfiguration;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.web.dto.UserDTO;
import com.camunda.fox.cycle.web.service.resource.UserService;

/**
 *
 * @author nico.rehwaldt
 */
@Path("secured/view/create-initial-user")
public class InitialConfigurationService extends AbstractRestService {
  
  @Inject
  private UserRepository userRepository;
  
  @Inject
  private CycleConfiguration configuration;
  
  @Inject
  private UserService userService;
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Object createInitialUser() {
    if (!canCreateInitialUser()) {
      return redirectTo("secured/view/index");
    }
    
    return "tpl:app/create-initial-user";
  }

  @POST
  @Produces(MediaType.TEXT_HTML)
  public UserDTO createInitialUser(UserDTO data) {
    if (!canCreateInitialUser()) {
      throw notAllowed("already configured");
    }
    
    if (data.getEmail() == null || data.getPassword() == null || data.getName() == null) {
      throw badRequest("invalid fields");
    }
    
    data.setAdmin(true);
    return userService.create(data);
  }
  
  private boolean canCreateInitialUser() {
    return !configuration.isUseJaas() && userRepository.countAll() == 0;
  }
}
