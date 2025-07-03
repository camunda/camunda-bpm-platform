package org.camunda.bpm.engine.test.history;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

public class TestEventHandler implements HistoryEventHandler {

  private final Queue<HistoryEvent> queue = new ArrayDeque<>(50);

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    queue.offer(historyEvent);
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    historyEvents.forEach(queue::offer);
  }

  public HistoryEvent poll() {
    return this.queue.poll();
  }

  public HistoryEvent peek() {
    return this.queue.peek();
  }
}
