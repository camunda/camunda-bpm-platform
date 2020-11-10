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
package org.camunda.bpm.admin.plugin.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.admin.impl.plugin.resources.MetricsRestService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsResultDto;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MetricsRestServiceTest extends AbstractAdminPluginTest {

  public static final SimpleDateFormat DATE_FORMAT =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  protected MetricsRestService resource;
  protected ProcessEngine processEngine;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void setUp() {
    super.before();
    processEngine = getProcessEngine();
    runtimeService = processEngine.getRuntimeService();
    resource = new MetricsRestService(processEngine.getName());
    ObjectMapper objectMapper = new ObjectMapper();
    resource.setObjectMapper(objectMapper);
    taskService = processEngine.getTaskService();
  }

  @After
  public void reset() {
    ClockUtil.reset();
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void shouldCountUniqueTaskWorkers_StartTimeInBetween() throws ParseException {
    // given
    Date currentTime = DATE_FORMAT.parse("2020-01-02T00:00:00.000+0100");
    ClockUtil.setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("userTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setAssignee(taskId, "foo");

    String startDateAsString = "2020-01-01T00:00:00.000+0100";
    String endDateAsString = "2020-01-03T00:00:00.000+0100";

    // when
    MetricsResultDto metricsResultDto =
      resource.countUniqueTaskWorkers(startDateAsString, endDateAsString);

    // then
    assertThat(metricsResultDto.getResult()).isEqualTo(1L);
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void shouldCountUniqueTaskWorkers_StartTimeBefore() throws ParseException {
    // given
    Date currentTime = DATE_FORMAT.parse("2019-12-31T00:00:00.000+0100");
    ClockUtil.setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("userTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setAssignee(taskId, "foo");

    String startDateAsString = "2020-01-01T00:00:00.000+0100";
    String endDateAsString = "2020-01-03T00:00:00.000+0100";

    // when
    MetricsResultDto metricsResultDto =
      resource.countUniqueTaskWorkers(startDateAsString, endDateAsString);

    // then
    assertThat(metricsResultDto.getResult()).isEqualTo(0L);
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void shouldCountUniqueTaskWorkers_StartTimeAfter() throws ParseException {
    // given
    Date currentTime = DATE_FORMAT.parse("2020-01-04T00:00:00.000+0100");
    ClockUtil.setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("userTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setAssignee(taskId, "foo");

    String startDateAsString = "2020-01-01T00:00:00.000+0100";
    String endDateAsString = "2020-01-03T00:00:00.000+0100";

    // when
    MetricsResultDto metricsResultDto =
      resource.countUniqueTaskWorkers(startDateAsString, endDateAsString);

    // then
    assertThat(metricsResultDto.getResult()).isEqualTo(0L);
  }

}
