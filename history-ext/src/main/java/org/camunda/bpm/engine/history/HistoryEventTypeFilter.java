package org.camunda.bpm.engine.history;

import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author jbellmann
 * 
 */
public final class HistoryEventTypeFilter implements HistoryEventFilter {

  private final List<Class<?>> types;
  private final boolean negate;

  public HistoryEventTypeFilter(List<Class<?>> classes) {
    this(classes, false);
  }

  public HistoryEventTypeFilter(List<Class<?>> classes, boolean negate) {
    this.types = ImmutableList.copyOf(classes);
    this.negate = negate;
  }

  @Override
  public boolean canPass(HistoryEvent historyEvent) {
    boolean contained = types.contains(historyEvent.getClass());
    return negate == false ? contained : !contained;
  }
}
