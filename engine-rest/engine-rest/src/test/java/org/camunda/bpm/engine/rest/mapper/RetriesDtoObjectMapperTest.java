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
package org.camunda.bpm.engine.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.RetriesDto;
import org.camunda.bpm.engine.rest.dto.runtime.SetJobRetriesByProcessDto;
import org.camunda.bpm.engine.rest.dto.runtime.SetJobRetriesDto;
import org.junit.Test;

public class RetriesDtoObjectMapperTest {

  @Test
  public void shouldExcludeDueDateSetPropertyRetriesDto() throws JsonProcessingException {
    // given
    RetriesDto dto = new RetriesDto();
    dto.setRetries(4);
    dto.setDueDate(new Date(1675752840000L));

    ObjectMapper mapper = new ObjectMapper();

    // when
    String json = mapper.writeValueAsString(dto);

    // then
    assertThat(json).doesNotContain("dueDateSet");
  }

  @Test
  public void shouldIncludeDueDateSetPropertyRetriesDto() throws JsonProcessingException {
    // given
    String json = "{"
        + "\"retries\":4,"
        + "\"dueDate\":1675752840000"
        + "}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    RetriesDto dto = mapper.readValue(json, RetriesDto.class);

    // then
    assertThat(dto.isDueDateSet()).isTrue();
  }

  @Test
  public void shouldIgnoreDueDateSetPropertyRetriesDto() throws JsonProcessingException {
    // given
    String json = "{"
        + "\"retries\":4"
        + "}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    RetriesDto dto = mapper.readValue(json, RetriesDto.class);

    // then
    assertThat(dto.isDueDateSet()).isFalse();
  }

  @Test
  public void shouldExcludeDueDateSetPropertySetJobRetriesDto() throws JsonProcessingException {
    // given
    SetJobRetriesDto dto = new SetJobRetriesDto();
    dto.setRetries(4);
    dto.setDueDate(new Date(1675752840000L));
    dto.setJobIds(new ArrayList<>());
    dto.setJobQuery(new JobQueryDto());

    ObjectMapper mapper = new ObjectMapper();

    // when
    String json = mapper.writeValueAsString(dto);

    // then
    assertThat(json).doesNotContain("dueDateSet");
  }

  @Test
  public void shouldIncludeDueDateSetPropertySetJobRetriesDto() throws JsonProcessingException {
    // given
    String json = "{"
        + "\"retries\":4,"
        + "\"dueDate\":1675752840000,"
        + "\"jobIds\":[],"
        + "\"jobQuery\":{\"sorting\":null}"
        + "}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    SetJobRetriesDto dto = mapper.readValue(json, SetJobRetriesDto.class);

    // then
    assertThat(dto.isDueDateSet()).isTrue();
  }

  @Test
  public void shouldIgnoreDueDateSetPropertySetJobRetriesDto() throws JsonProcessingException {
    // given
    String json = "{"
        + "\"retries\":4,"
        + "\"jobIds\":[],"
        + "\"jobQuery\":{\"sorting\":null}"
        + "}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    SetJobRetriesDto dto = mapper.readValue(json, SetJobRetriesDto.class);

    // then
    assertThat(dto.isDueDateSet()).isFalse();
  }

  @Test
  public void shouldExcludeDueDateSetPropertySetJobRetriesByProcessDto() throws JsonProcessingException {
    // given
    SetJobRetriesByProcessDto dto = new SetJobRetriesByProcessDto();
    dto.setRetries(4);
    dto.setDueDate(new Date(1675752840000L));
    dto.setHistoricProcessInstanceQuery(new HistoricProcessInstanceQueryDto());
    dto.setProcessInstanceQuery(new ProcessInstanceQueryDto());
    dto.setProcessInstances(new ArrayList<>());

    ObjectMapper mapper = new ObjectMapper();

    // when
    String json = mapper.writeValueAsString(dto);

    // then
    assertThat(json).doesNotContain("dueDateSet");
  }

  @Test
  public void shouldIncludeDueDateSetPropertySetJobRetriesByProcessDto() throws JsonProcessingException {
    // given
    String json = "{"
        + "\"retries\":4,"
        + "\"dueDate\":1675752840000,"
        + "\"jobIds\":null,"
        + "\"jobQuery\":null,"
        + "\"processInstances\":[],"
        + "\"historicProcessInstanceQuery\":{\"processDefinitionId\":null,\"incidentType\":null,\"orQueries\":null,\"sorting\":null}"
        + "}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    SetJobRetriesByProcessDto dto = mapper.readValue(json, SetJobRetriesByProcessDto.class);

    // then
    assertThat(dto.isDueDateSet()).isTrue();
  }

  @Test
  public void shouldIgnoreDueDateSetPropertySetJobRetriesByProcessDto() throws JsonProcessingException {
    // given
    String json = "{\"retries\":4,\"jobIds\":null,\"jobQuery\":null,\"processInstances\":[],\"processInstanceQuery\":{\"deploymentId\":null,\"processDefinitionKey\":null,\"processDefinitionKeys\":null,\"processDefinitionKeyNotIn\":null,\"businessKey\":null,\"businessKeyLike\":null,\"caseInstanceId\":null,\"processDefinitionId\":null,\"superProcessInstance\":null,\"subProcessInstance\":null,\"superCaseInstance\":null,\"subCaseInstance\":null,\"active\":null,\"suspended\":null,\"processInstanceIds\":null,\"withIncident\":null,\"incidentId\":null,\"incidentType\":null,\"incidentMessage\":null,\"incidentMessageLike\":null,\"withoutTenantId\":null,\"activityIds\":null,\"rootProcessInstances\":null,\"leafProcessInstances\":null,\"variableNamesIgnoreCase\":null,\"variableValuesIgnoreCase\":null,\"variables\":null,\"orQueries\":null,\"processDefinitionWithoutTenantId\":null,\"tenantIdIn\":null,\"sorting\":null},\"historicProcessInstanceQuery\":{\"processDefinitionId\":null,\"incidentType\":null,\"orQueries\":null,\"sorting\":null}}";

    ObjectMapper mapper = new ObjectMapper();

    // when
    SetJobRetriesByProcessDto dto = mapper.readValue(json, SetJobRetriesByProcessDto.class);

    // then
    assertThat(dto.isDueDateSet()).isFalse();
  }
}
