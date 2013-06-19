package org.camunda.bpm.engine.history.marshaller;

import java.io.InputStream;

/**
 * @author  jbellmann
 */
public class EventResources {

    public static final String HISTORY_EVENT = "historicEvent.xml";

    public static final String HISTORIC_ACTIVITY_INSTANCE_EVENT_ENTITY = "historicActivityInstanceEventEntity.xml";

    public static InputStream getHistoryEventAsInputStream() {
        return EventResources.class.getResourceAsStream(HISTORY_EVENT);
    }

    public static InputStream getHistoricActivityInstanceEventEntityAsStream() {
        return EventResources.class.getResourceAsStream(HISTORIC_ACTIVITY_INSTANCE_EVENT_ENTITY);
    }

}
