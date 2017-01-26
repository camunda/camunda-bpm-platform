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
package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.rest.dto.identity.BasicUserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.task.GroupInfoDto;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public interface IdentityRestService {

  public static final String PATH = "/identity";

  @GET
  @Path("/groups")
  @Produces(MediaType.APPLICATION_JSON)
  GroupInfoDto getGroupInfo(@QueryParam("userId") String userId);

  @POST
  @Path("/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  AuthenticationResult verifyUser(BasicUserCredentialsDto credentialsDto);

}
