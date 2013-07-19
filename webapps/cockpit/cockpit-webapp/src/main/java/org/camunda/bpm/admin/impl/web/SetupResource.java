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
package org.camunda.bpm.admin.impl.web;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.impl.UserRestServiceImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

/**
 * <p>Jax RS resource allowing to perform the setup steps.</p>
 * 
 * <p>All methods of this class must throw Status.FORBIDDEN exception if 
 * setup actions are unavailable.</p>
 * 
 * @author Daniel Meyer
 *
 */
@Path("/setup/{engine}")
public class SetupResource {

  @Path("/user/create")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void createInitialUser(@PathParam("engine") String processEngineName, UserDto user) {
    
    // make sure we can process this request at this time
    ensureSetupAvailable(lookupProcessEngine(processEngineName));    
    
    // reuse logic from rest api implementation
    UserRestServiceImpl userRestServiceImpl = new UserRestServiceImpl(processEngineName);
    userRestServiceImpl.createUser(user);
  }
  
  protected void ensureSetupAvailable(ProcessEngine processEngine) {
    if(processEngine.getIdentityService().isReadOnly() 
        || (processEngine.getIdentityService().createUserQuery().count() > 0)) {
      
      throw new InvalidRequestException(Status.FORBIDDEN, "Setup action not available");
      
    }
  }

  protected ProcessEngine lookupProcessEngine(String engineName) {
    
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine(engineName);
      
    } else {
      throw new RestException(Status.BAD_REQUEST, "Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");
      
    }

  }

  
}
