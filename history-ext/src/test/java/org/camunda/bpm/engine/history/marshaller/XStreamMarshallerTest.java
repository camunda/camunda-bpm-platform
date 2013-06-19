package org.camunda.bpm.engine.history.marshaller;

import org.camunda.bpm.engine.history.EventBuilder;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author  jbellmann
 */
public class XStreamMarshallerTest {

    private static final Logger LOG = LoggerFactory.getLogger(XStreamMarshallerTest.class);

    private XStream xstream = new XStream();

    private HistoryEventHandler historyEventHandlerMock = Mockito.mock(HistoryEventHandler.class);

    @Test
    public void marshallFromHistoryEventToStringAndBack() {

        HistoryEvent event = EventBuilder.buildHistoryEvent();

        String eventAsString = xstream.toXML(event);

        HistoryEvent fromXml = (HistoryEvent) xstream.fromXML(eventAsString);

        assertHistoryEvent(event, fromXml);

        Assert.assertEquals(event.getTimestamp(), fromXml.getTimestamp());

        assertHandler(event);
        assertHandler(fromXml);

        LOG.info(eventAsString);

    }

    @Test
    public void marshallFromHistoricActivityInstanceEventToStringAndBack() {

        HistoricActivityInstanceEventEntity event = EventBuilder.buildHistoricActivityInstanceEventEntity();

        String eventAsString = xstream.toXML(event);

        HistoricActivityInstanceEventEntity fromXml = (HistoricActivityInstanceEventEntity) xstream.fromXML(
                eventAsString);

        assertHistoryEvent(event, fromXml);
        assertHistoricActivityInstanceEventEntity(event, fromXml);

        Assert.assertEquals(event.getTimestamp(), fromXml.getTimestamp());

        assertHandler(event);
        assertHandler(fromXml);

        LOG.info(eventAsString);

    }

    @Test
    public void marshallHistoricActivityInstanceEventEntityFromFile() {
        HistoricActivityInstanceEventEntity fromXml = (HistoricActivityInstanceEventEntity) xstream.fromXML(
                EventResources.getHistoricActivityInstanceEventEntityAsStream());

        HistoricActivityInstanceEventEntity event = EventBuilder.buildHistoricActivityInstanceEventEntity();

        assertHistoryEvent(event, fromXml);
        assertHistoricActivityInstanceEventEntity(event, fromXml);

        assertHandler(event);
        assertHandler(fromXml);

    }

    @Test
    public void marshallHistoryEventFromFile() {
        HistoryEvent fromXml = (HistoryEvent) xstream.fromXML(EventResources.getHistoryEventAsInputStream());

        HistoryEvent event = EventBuilder.buildHistoryEvent();

        assertHistoryEvent(event, fromXml);

        assertHandler(event);
        assertHandler(fromXml);

    }

    protected <T extends HistoryEvent> void assertHandler(final T event) {
        historyEventHandlerMock.handleEvent(event);
    }

    protected void assertHistoryEvent(final HistoryEvent event, final HistoryEvent fromXml) {
        Assert.assertEquals(event.getId(), fromXml.getId());
        Assert.assertEquals(event.getExecutionId(), fromXml.getExecutionId());
        Assert.assertEquals(event.getProcessDefinitionId(), fromXml.getProcessDefinitionId());
        Assert.assertEquals(event.getProcessInstanceId(), fromXml.getProcessInstanceId());
    }

    protected void assertHistoricActivityInstanceEventEntity(final HistoricActivityInstanceEventEntity event,
            final HistoricActivityInstanceEventEntity fromXml) {
        Assert.assertEquals(event.getActivityId(), fromXml.getActivityId());
        Assert.assertEquals(event.getActivityInstanceId(), fromXml.getActivityInstanceId());
        Assert.assertEquals(event.getActivityName(), fromXml.getActivityName());
        Assert.assertEquals(event.getActivityType(), fromXml.getActivityType());
    }

}
