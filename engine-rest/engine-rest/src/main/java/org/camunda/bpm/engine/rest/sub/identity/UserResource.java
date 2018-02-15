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
package org.camunda.bpm.engine.rest.sub.identity;

import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * @author Daniel Meyer
 *
 */
public interface UserResource {

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteUser();

  @GET
  @Path("/unlock")
  public void unlockUser();

  // profile ///////////////////

  @GET
  @Path("/profile")
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileDto getUserProfile(@Context UriInfo context);

  @PUT
  @Path("/profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateProfile(UserProfileDto profile);

  // credentials //////////////

  @PUT
  @Path("/credentials")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateCredentials(UserCredentialsDto account);

  @OPTIONS
  @Produces(MediaType.APPLICATION_JSON)
  ResourceOptionsDto availableOperations(@Context UriInfo context);

}
