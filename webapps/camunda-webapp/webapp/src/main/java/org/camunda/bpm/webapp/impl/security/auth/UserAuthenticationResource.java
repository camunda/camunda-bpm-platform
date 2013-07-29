/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.auth;

import static org.camunda.bpm.engine.authorization.Permissions.*;
import static org.camunda.bpm.engine.authorization.Resources.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

/**
 * Jax-Rs resource allowing users to authenticate with username and password</p>
 * 
 * @author Daniel Meyer
 * 
 */
@Path(UserAuthenticationResource.PATH)
public class UserAuthenticationResource {

  public static final String PATH = "/auth/user";
  
  @Context
  protected HttpServletRequest request;

  @POST
  @Path("/{processEngineName}/login/{appName}")  
  public Response doLogin(
      @PathParam("processEngineName") String engineName,
      @PathParam("appName") String appName, 
      @FormParam("username") String username, 
      @FormParam("password") String password) {
    
    final ProcessEngine processEngine = lookupProcessEngine(engineName);
    if(processEngine == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Process engine with name "+engineName+" does not exisist");
    }
    
    // check password / username
    boolean isPasswordValid = processEngine.getIdentityService().checkPassword(username, password);

    if (!isPasswordValid) {
      return Response.status(Status.UNAUTHORIZED).build();

    } else {           
      
      // get user's groups      
      final List<Group> groupList = processEngine.getIdentityService().createGroupQuery()
        .groupMember(username)
        .list();
      
      // transform into list of strings:
      List<String> groupIds = new ArrayList<String>();
      for (Group group : groupList) {
        groupIds.add(group.getId());
      }
      
      // check user's app authorizations
      AuthorizationService authorizationService = processEngine.getAuthorizationService();
      boolean tasklistAuthorized = authorizationService.isUserAuthorized(username, groupIds, ACCESS, APPLICATION, "tasklist");
      boolean cockpitAuthorized = authorizationService.isUserAuthorized(username, groupIds, ACCESS, APPLICATION, "cockpit");
      
      if(appName.equals("tasklist") && !tasklistAuthorized) {
        return Response.status(Status.UNAUTHORIZED).build();
        
      } else if(appName.equals("cockpit") && !cockpitAuthorized) {
        return Response.status(Status.UNAUTHORIZED).build();
        
      }
      
      final Authentications authentications = Authentications.getCurrent();
              
      // create new authentication
      UserAuthentication newAuthentication = new UserAuthentication(username, groupIds, engineName, tasklistAuthorized, cockpitAuthorized);      
      authentications.addAuthentication(newAuthentication);      
      
      // send reponse including updated cookie.
      return createAuthCookie(Response.ok(), authentications)
        .entity(AuthenticationDto.fromAuthentication(newAuthentication))
        .build();
    }
  }

  
  @POST
  @Path("/{processEngineName}/logout") 
  public Response doLogout(@PathParam("processEngineName") String engineName) {
    final Authentications authentications = Authentications.getCurrent();
    
    // remove authentication for process engine
    authentications.removeAuthenticationForProcessEngine(engineName);
    
    // send reponse including updated cookie.
    return createAuthCookie(Response.ok(), authentications).build();
  }
  
  /** constructs a new authentication cookie and returns the response. 
   * @param responseBuilder */
  protected ResponseBuilder createAuthCookie(ResponseBuilder responseBuilder, Authentications authentications) {
    NewCookie cookie = AuthenticationCookie.fromAuthentications(authentications, request);      
    return responseBuilder.cookie(cookie);
  }
    
  protected ProcessEngine lookupProcessEngine(String engineName) {
    
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine(engineName);
      
    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");
      
    }

  }

}
