package org.camunda.bpm.engine.history;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.history.marshaller.EventBuilder;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author jbellmann
 * 
 */
public class HistoryEventFilterTest {

  @Test
  public void defaultHistoryFilter() {
    List<Class<?>> clazzez = new ArrayList<Class<?>>();
    clazzez.add(HistoricActivityInstanceEventEntity.class);
    HistoryEventFilter filter = new HistoryEventTypeFilter(clazzez);
    Assert.assertTrue("HistoricActivityInstanceEventEntity should pass the filter", filter.canPass(EventBuilder.buildHistoricActivityInstanceEventEntity()));
  }

  @Test
  public void negatedHistoryFilter() {
    List<Class<?>> clazzez = new ArrayList<Class<?>>();
    clazzez.add(HistoricActivityInstanceEventEntity.class);
    clazzez.add(HistoryEvent.class);
    HistoryEventFilter filter = new HistoryEventTypeFilter(clazzez, true);
    Assert.assertFalse("HistoricActivityInstanceEventEntity should not pass the negated filter",
        filter.canPass(EventBuilder.buildHistoricActivityInstanceEventEntity()));
    Assert.assertFalse("HistoryEvent should not pass the negated filter", filter.canPass(EventBuilder.buildHistoryEvent()));
  }
}
