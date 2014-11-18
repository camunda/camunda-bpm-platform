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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.rest.dto.identity.UserQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.UserResource;
import org.camunda.bpm.engine.rest.sub.identity.impl.UserResourceImpl;
import org.camunda.bpm.engine.rest.util.PathUtil;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Resources.USER;

/**
 * @author Daniel Meyer
 *
 */
public class UserRestServiceImpl extends AbstractAuthorizedRestResource implements UserRestService {

  public UserRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, USER, ANY, objectMapper);
  }

  public UserResource getUser(String id) {
    id = PathUtil.decodePathParam(id);
    return new UserResourceImpl(getProcessEngine().getName(), id, relativeRootResourcePath, getObjectMapper());
  }

  public List<UserProfileDto> queryUsers(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    UserQueryDto queryDto = new UserQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryUsers(queryDto, firstResult, maxResults);
  }

  public List<UserProfileDto> queryUsers(UserQueryDto queryDto, Integer firstResult, Integer maxResults) {

    queryDto.setObjectMapper(getObjectMapper());
    UserQuery query = queryDto.toQuery(getProcessEngine());

    List<User> resultList;
    if(firstResult != null || maxResults != null) {
      resultList = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      resultList = query.list();
    }

    return UserProfileDto.fromUserList(resultList);
  }


  public CountResultDto getUserCount(UriInfo uriInfo) {
    UserQueryDto queryDto = new UserQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return getUserCount(queryDto);
  }

  protected CountResultDto getUserCount(UserQueryDto queryDto) {
    UserQuery query = queryDto.toQuery(getProcessEngine());
    long count = query.count();
    return new CountResultDto(count);
  }

  public void createUser(UserDto userDto) {
    final IdentityService identityService = getIdentityService();

    if(identityService.isReadOnly()) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Identity service implementation is read-only.");
    }

    UserProfileDto profile = userDto.getProfile();
    if(profile == null || profile.getId() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "request object must provide profile information with valid id.");
    }

    User newUser = identityService.newUser(profile.getId());
    profile.update(newUser);

    if(userDto.getCredentials() != null) {
      newUser.setPassword(userDto.getCredentials().getPassword());
    }

    identityService.saveUser(newUser);

  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    final IdentityService identityService = getIdentityService();

    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(UserRestService.PATH);

    ResourceOptionsDto resourceOptionsDto = new ResourceOptionsDto();

    // GET /
    URI baseUri = baseUriBuilder.build();
    resourceOptionsDto.addReflexiveLink(baseUri, HttpMethod.GET, "list");

    // GET /count
    URI countUri = baseUriBuilder.clone().path("/count").build();
    resourceOptionsDto.addReflexiveLink(countUri, HttpMethod.GET, "count");

    // POST /create
    if(!identityService.isReadOnly() && isAuthorized(CREATE)) {
      URI createUri = baseUriBuilder.clone().path("/create").build();
      resourceOptionsDto.addReflexiveLink(createUri, HttpMethod.POST, "create");
    }

    return resourceOptionsDto;
  }

  // utility methods //////////////////////////////////////

  protected List<User> executePaginatedQuery(UserQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

}
