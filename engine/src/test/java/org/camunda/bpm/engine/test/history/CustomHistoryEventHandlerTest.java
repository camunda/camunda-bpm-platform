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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class CustomHistoryEventHandlerTest {

  protected static RecorderHistoryEventHandler recorderHandler = new RecorderHistoryEventHandler();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(c -> {
    c.setHistoryEventHandler(recorderHandler);
  });

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  private RuntimeService runtimeService;
  private TaskService taskService;

  @After
  public void clearHistoryEvents() {
    recorderHandler.clear();
  }

  @Before
  public void initServices() {
    recorderHandler.clear();

    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldReceiveMigrateEvents() {
    // given
    VariableMap variables = Variables.createVariables().putValue("foo", "bar");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    String processDefinitionId = processInstance.getProcessDefinitionId();
    String processInstanceId = processInstance.getId();

    Task task = taskService.createTaskQuery().singleResult();
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId).getActivityInstances("theTask")[0];

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(processDefinitionId, processDefinitionId).mapEqualActivities().build();
    recorderHandler.clear();

    // when
    runtimeService.newMigration(migrationPlan).processInstanceIds(processInstanceId).execute();

    // then
    // one process instance, one activity instance, one task instance, one variable instance
    assertThat(recorderHandler.size()).isEqualTo(4);

    List<HistoryEvent> processInstanceEvents = recorderHandler.getEventsForEntity(processInstanceId);
    assertThat(processInstanceEvents).hasSize(1);
    assertThat(processInstanceEvents.get(0).getEventType()).isEqualTo(HistoryEventTypes.PROCESS_INSTANCE_MIGRATE.getEventName());

    List<HistoryEvent> activityInstanceEvents = recorderHandler.getEventsForEntity(activityInstance.getId());
    assertThat(activityInstanceEvents).hasSize(1);
    assertThat(activityInstanceEvents.get(0).getEventType()).isEqualTo(HistoryEventTypes.ACTIVITY_INSTANCE_MIGRATE.getEventName());

    List<HistoryEvent> taskEvents = recorderHandler.getEventsForEntity(task.getId());
    assertThat(taskEvents).hasSize(1);
    assertThat(taskEvents.get(0).getEventType()).isEqualTo(HistoryEventTypes.TASK_INSTANCE_MIGRATE.getEventName());

    List<HistoryEvent> variableEvents = recorderHandler.getEventsForEntity(null); // variable events currently have no id set
    assertThat(variableEvents).hasSize(1);
    assertThat(variableEvents.get(0).getEventType()).isEqualTo(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE.getEventName());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldReceiveMigrateEventForIncident() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processDefinitionId = processInstance.getProcessDefinitionId();
    String processInstanceId = processInstance.getId();

    Incident incident = runtimeService.createIncident("foo", processInstanceId, "bar");

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(processDefinitionId, processDefinitionId).mapEqualActivities().build();
    recorderHandler.clear();

    // when
    runtimeService.newMigration(migrationPlan).processInstanceIds(processInstanceId).execute();

    // then
    List<HistoryEvent> incidentEvents = recorderHandler.getEventsForEntity(incident.getId());
    assertThat(incidentEvents).hasSize(1);
    assertThat(incidentEvents.get(0).getEventType()).isEqualTo(HistoryEventTypes.INCIDENT_MIGRATE.getEventName());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldReceiveHistoricProcessInstanceEventFirstOnFormSubmit() {
    // given
    FormService formService = engineRule.getFormService();
    RepositoryService repositoryService = engineRule.getRepositoryService();
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap formData = Variables.createVariables().putValue("foo", "bar");

    // when
    formService.submitStartForm(procDef.getId(), formData);

    // then
    List<HistoryEvent> historyEvents = recorderHandler.getEvents().stream()
        .filter(h -> h instanceof HistoricVariableUpdateEventEntity ||
            h instanceof HistoricProcessInstanceEventEntity)
        .collect(Collectors.toList());

    assertThat(historyEvents).hasSize(2);
    HistoryEvent processInstanceEvent = historyEvents.get(0);
    HistoryEvent variableInstanceEvent = historyEvents.get(1);

    assertThat(processInstanceEvent).isInstanceOf(HistoricProcessInstanceEventEntity.class);
    assertThat(processInstanceEvent.getEventType()).isEqualTo("start");

    assertThat(variableInstanceEvent).isInstanceOf(HistoricVariableUpdateEventEntity.class);
    assertThat(variableInstanceEvent.getEventType()).isEqualTo("create");
  }

  public static class RecorderHistoryEventHandler implements HistoryEventHandler {

    private Map<String, List<HistoryEvent>> historyEventsByEntity = new HashMap<>();
    private List<HistoryEvent> historyEvents = new ArrayList<>();

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
      CollectionUtil.addToMapOfLists(historyEventsByEntity, historyEvent.getId(), historyEvent);
      historyEvents.add(historyEvent);
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
      historyEvents.forEach(this::handleEvent);
    }

    public void clear() {
      historyEventsByEntity.clear();
      historyEvents.clear();
    }

    public List<HistoryEvent> getEventsForEntity(String id) {
      return historyEventsByEntity.getOrDefault(id, Collections.emptyList());
    }

    public List<HistoryEvent> getEvents() {
      return historyEvents;
    }

    public int size() {
      return historyEvents.size();
    }
  }
}
