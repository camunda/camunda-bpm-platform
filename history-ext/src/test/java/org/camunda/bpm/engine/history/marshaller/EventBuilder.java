package org.camunda.bpm.engine.history.marshaller;

import java.util.Date;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * Builds some events for testing.
 *
 * @author  jbellmann
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

    public static <T extends HistoryEvent> T fillHistoricEventData(final T event) {
        event.setId("TEST_ID");
        event.setExecutionId("TEST_EXECUTION_ID");
        event.setProcessDefinitionId("TEST_PROCESSDEFINITION_ID");
        event.setProcessInstanceId("TEST_PROCESSINSTANCE_ID");
        event.setTimestamp(new Date());
        return event;
    }

    public static <T extends HistoricActivityInstanceEventEntity> T fillHistoricActivityInstanceEventEntity(
            final T event) {
        event.setActivityId("TEST_ACTIVITY_ID");
        event.setActivityInstanceId("TEST_ACTIVITY_ID");
        event.setActivityName("TEST_ACTIVITY_NAME");
        event.setActivityType("TEST_ACTIVITY_TYPE");
        event.setEventType("TEST_EVENT_TYPE");
        event.setParentActivityInstanceId("TEST_PARENT_ACTIVITY_INSTANCE_ID");
        return event;
    }

}
