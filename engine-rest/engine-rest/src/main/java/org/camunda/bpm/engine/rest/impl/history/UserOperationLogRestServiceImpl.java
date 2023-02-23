/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.rest.dto.AnnotationDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogQueryDto;
import org.camunda.bpm.engine.rest.history.UserOperationLogRestService;
import org.camunda.bpm.engine.rest.util.QueryUtil;

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
    return UserOperationLogEntryDto.map(QueryUtil.list(query, firstResult, maxResults));
  }

  @Override
  public Response setAnnotation(String operationId, AnnotationDto annotationDto) {
    String annotation = annotationDto.getAnnotation();

    processEngine.getHistoryService()
        .setAnnotationForOperationLogById(operationId, annotation);

    return Response.noContent().build();
  }

  @Override
  public Response clearAnnotation(String operationId) {
    processEngine.getHistoryService()
        .clearAnnotationForOperationLogById(operationId);

    return Response.noContent().build();
  }

}
