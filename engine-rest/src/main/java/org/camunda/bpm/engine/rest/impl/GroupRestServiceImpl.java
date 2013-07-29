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

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.GroupRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.identity.GroupDto;
import org.camunda.bpm.engine.rest.dto.identity.GroupQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.GroupResource;
import org.camunda.bpm.engine.rest.sub.identity.impl.GroupResourceImpl;

/**
 * @author Daniel Meyer
 *
 */
public class GroupRestServiceImpl extends AbstractRestProcessEngineAware implements GroupRestService {
  
  public GroupRestServiceImpl() {
    super();
  }

  public GroupRestServiceImpl(String engineName) {
    super(engineName);
  }

  public GroupResource getGroup(String id) {
    return new GroupResourceImpl(getProcessEngine(), id, relativeRootResourcePath);
  }

  public List<GroupDto> queryGroups(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    GroupQueryDto queryDto = new GroupQueryDto(uriInfo.getQueryParameters());
    return queryGroups(queryDto, firstResult, maxResults);
  }

  public List<GroupDto> queryGroups(GroupQueryDto queryDto, Integer firstResult, Integer maxResults) {
    
    GroupQuery query = queryDto.toQuery(getProcessEngine());
    
    List<Group> resultList;
    if(firstResult != null || maxResults != null) {
      resultList = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      resultList = query.list();
    }
    
    return GroupDto.fromGroupList(resultList);
  }

  public CountResultDto getGroupCount(UriInfo uriInfo) {
    GroupQueryDto queryDto = new GroupQueryDto(uriInfo.getQueryParameters());
    return getGroupCount(queryDto);
  }

  protected CountResultDto getGroupCount(GroupQueryDto queryDto) {
    GroupQuery query = queryDto.toQuery(getProcessEngine());    
    long count = query.count();
    return new CountResultDto(count);
  }

  public void createGroup(GroupDto groupDto) {
    final IdentityService identityService = getIdentityService();
    
    if(identityService.isReadOnly()) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Identity service implementation is read-only.");
    }
    
    Group newGroup = identityService.newGroup(groupDto.getId());
    groupDto.update(newGroup);
    identityService.saveGroup(newGroup);
      
  }
  
  // utility methods //////////////////////////////////////
  
  protected List<Group> executePaginatedQuery(GroupQuery query, Integer firstResult, Integer maxResults) {
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
