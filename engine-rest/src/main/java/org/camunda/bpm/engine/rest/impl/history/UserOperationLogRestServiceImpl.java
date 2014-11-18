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
package org.camunda.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogQueryDto;
import org.camunda.bpm.engine.rest.history.UserOperationLogRestService;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Danny Gräf
 */
public class UserOperationLogRestServiceImpl implements UserOperationLogRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public UserOperationLogRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public CountResultDto queryUserOperationCount(UriInfo uriInfo) {
    UserOperationLogQueryDto queryDto = new UserOperationLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    UserOperationLogQuery query = queryDto.toQuery(processEngine);
    return new CountResultDto(query.count());
  }

  @Override
  public List<UserOperationLogEntryDto> queryUserOperationEntries(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    UserOperationLogQueryDto queryDto = new UserOperationLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    UserOperationLogQuery query = queryDto.toQuery(processEngine);

    if (firstResult == null && maxResults == null) {
      return UserOperationLogEntryDto.map(query.list());
    } else {
      if (firstResult == null) {
        firstResult = 0;
      }
      if (maxResults == null) {
        maxResults = Integer.MAX_VALUE;
      }
      return UserOperationLogEntryDto.map(query.listPage(firstResult, maxResults));
    }
  }
}
