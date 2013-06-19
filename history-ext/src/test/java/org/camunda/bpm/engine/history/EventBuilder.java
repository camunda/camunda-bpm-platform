package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * Builds some events for testing.
 * 
 * @author jbellmann
 */
public class EventBuilder {

  public static HistoricActivityInstanceEventEntity buildHistoricActivityInstanceEventEntity() {

    HistoricActivityInstanceEventEntity event = new HistoricActivityInstanceEventEntity();

    event = fillHistoricEventData(event);
    event = fillHistoricActivityInstanceEventEntity(event);

    return event;
  }

  public static HistoryEvent buildHistoryEvent() {
    HistoryEvent event = new HistoryEvent();
    event = fillHistoricEventData(event);
    return event;
  }

  public static HistoricProcessInstanceEventEntity buildHistoricProcessInstanceEventEntity() {

    HistoricProcessInstanceEventEntity event = new HistoricProcessInstanceEventEntity();
    event = fillHistoricEventData(event);
    event = fillHistoricProcessInstanceEventEntity(event);
    return event;
  }

  public static HistoricTaskInstanceEventEntity buildHistoricTaskInstanceEventEntity() {

    HistoricTaskInstanceEventEntity event = new HistoricTaskInstanceEventEntity();

    event = fillHistoricEventData(event);
    event = fillHistoricTaskInstanceEventEntity(event);

    return event;
  }

  public static HistoricVariableUpdateEventEntity buildHistoricVariableUpdateEventEntity() {

    HistoricVariableUpdateEventEntity event = new HistoricVariableUpdateEventEntity();
    event = fillHistoricEventData(event);
    event = fillHistoricDetailEventEntity(event);
    event = fillHistoricVariableUpdateEventEntity(event);

    return event;
  }

  public static <T extends HistoryEvent> T fillHistoricEventData(final T event) {
    event.setId("TEST_ID");
    event.setExecutionId("TEST_EXECUTION_ID");
    event.setProcessDefinitionId("TEST_PROCESSDEFINITION_ID");
    event.setProcessInstanceId("TEST_PROCESSINSTANCE_ID");
    event.setTimestamp(new Date());
    return event;
  }

  public static <T extends HistoricDetailEventEntity> T fillHistoricDetailEventEntity(T event) {
    event.setActivityInstanceId("TEST_ACTIVITY_INSTANCE_ID");
    event.setTaskId("TEST_TASK_ID");
    return event;
  }

  public static <T extends HistoricActivityInstanceEventEntity> T fillHistoricActivityInstanceEventEntity(final T event) {
    event.setActivityId("TEST_ACTIVITY_ID");
    event.setActivityInstanceId("TEST_ACTIVITY_ID");
    event.setActivityName("TEST_ACTIVITY_NAME");
    event.setActivityType("TEST_ACTIVITY_TYPE");
    event.setEventType("TEST_EVENT_TYPE");
    event.setParentActivityInstanceId("TEST_PARENT_ACTIVITY_INSTANCE_ID");
    return event;
  }

  public static <T extends HistoricProcessInstanceEventEntity> T fillHistoricProcessInstanceEventEntity(T event) {
    event.setActivityId("TEST_ACTIVITY_ID");
    event.setBusinessKey("TEST_BUSINESS_KEY");
    event.setStartUserId("TEST_START_USER_ID");
    event.setSuperProcessInstanceId("TEST_SUPER_PROCESS_INSTANCE_ID");
    event.setDeleteReason("TEST_DELETE_REASON");
    return event;
  }

  public static <T extends HistoricTaskInstanceEventEntity> T fillHistoricTaskInstanceEventEntity(T event) {
    event.setTaskId("TEST_TASK_ID");
    event.setAssignee("TEST_ASSIGNEE");
    event.setOwner("TEST_OWNER");
    event.setName("TEST_NAME");
    event.setDescription("TEST_DESCRIPTION");
    event.setDueDate(new Date());
    event.setPriority(0);
    event.setParentTaskId("TEST_PARENT_TASK_ID");
    event.setDeleteReason("TEST_DELETE_REASON");
    event.setTaskDefinitionKey("TEST_TASK_DEFINITION_KEY");
    return event;
  }

  public static <T extends HistoricVariableUpdateEventEntity> T fillHistoricVariableUpdateEventEntity(T event) {

    event.setVariableInstanceId("TEST_VARIABLE_INSTANCE_ID");
    event.setVariableName("TEST_NAME");
    event.setRevision(1);
    event.setVariableTypeName("TEST_VARIABLE_TYPE_NAME");
    event.setLongValue(42L);
    event.setDoubleValue(23.12);
    event.setTextValue("TEST_TEXT_VALUE_ONE");
    event.setTextValue("TEST_TEXT_VALUE_TWO");
    event.setByteArrayId("TEST_BYTE_ARRAY_ID");
    event.setByteValue("TEST_BYTE_ARRAY".getBytes());

    return event;
  }

}
