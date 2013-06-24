package org.camunda.bpm.engine.history;

import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Dispatches the {@link HistoryEvent}s through all registered
 * {@link HistoryEventHandler}s.
 * 
 * @author jbellmann
 */
public class HistoryEventHandlerComposite implements HistoryEventHandler {

  private List<HistoryEventHandler> eventHandlerList = Lists.newLinkedList();

  private HistoryEventFilter historyEventFilter = new DefaultPassHistoryEventFilter();

  public HistoryEventHandlerComposite(final List<HistoryEventHandler> historyEventHandlers) {

    Preconditions.checkNotNull(historyEventHandlers, "List of HistoryEventHandler should never be null");
    this.eventHandlerList.addAll(Lists.newArrayList(Iterables.filter(historyEventHandlers, Predicates.notNull())));
  }

  public void setHistoryEventHandlers(final List<HistoryEventHandler> historyEventHandlers) {

    Preconditions.checkNotNull(historyEventHandlers, "List of HistoryEventHandler should never be null");
    this.eventHandlerList = Lists.newArrayList(Iterables.filter(historyEventHandlers, Predicates.notNull()));
  }

  public void setHistoryEventFilter(HistoryEventFilter historyEventFilter) {

    Preconditions.checkNotNull(historyEventFilter, "HistoryEventFilter should never be null");
    this.historyEventFilter = historyEventFilter;
  }

  public void handleEvent(final HistoryEvent historyEvent) {

    if (passEventsToListener(historyEvent)) {

      for (HistoryEventHandler handler : this.eventHandlerList) {
        handler.handleEvent(historyEvent);
      }
    }
  }

  protected boolean passEventsToListener(HistoryEvent historyEvent) {

    return this.historyEventFilter.canPass(historyEvent);
  }
}
