package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for Marshalling-Tests.
 * 
 * @author jbellmann
 * 
 */
public abstract class AbstractMarshallingTest {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractMarshallingTest.class);

  @Test
  public void marshallHistoricTaskInstanceEventEntity() throws Exception {

    HistoricTaskInstanceEventEntity entity = EventBuilder.buildHistoricTaskInstanceEventEntity();
    testMarshalling(new HistoryEventMessage(entity));
  }

  @Test
  public void marshallHistoricProcessInstanceEventEntity() throws Exception {

    HistoricProcessInstanceEventEntity entity = EventBuilder.buildHistoricProcessInstanceEventEntity();
    testMarshalling(new HistoryEventMessage(entity));
  }

  @Test
  public void marshallHistoricActivityInstanceEventEntity() throws Exception {

    HistoricActivityInstanceEventEntity entity = EventBuilder.buildHistoricActivityInstanceEventEntity();
    testMarshalling(new HistoryEventMessage(entity));
  }

  @Test
  public void marshallHistoricVariableUpdateEventEntity() throws Exception {

    HistoricVariableUpdateEventEntity entity = EventBuilder.buildHistoricVariableUpdateEventEntity();
    testMarshalling(new HistoryEventMessage(entity));
  }

  protected void testMarshalling(HistoryEventMessage entity) throws Exception {

    String marshallResult = marshall(entity);
    LOG.debug("Marshalled Event : {}", marshallResult);
    HistoryEventMessage unmarshallResult = unmarshall(marshallResult);
    assertHistoryEvent(entity.getHistoryEvent(), unmarshallResult.getHistoryEvent());
  }

  // TODO check this with Daniel, should we better implement equals on
  // HistoryEvent?
  protected void assertHistoryEvent(HistoryEvent entity, HistoryEvent unmarshallResult) {

    Assert.assertTrue(entity.getId().equals(unmarshallResult.getId()));
  }

  public abstract HistoryEventMessage unmarshall(String source) throws Exception;

  public abstract String marshall(HistoryEventMessage historyEvent) throws Exception;
}
