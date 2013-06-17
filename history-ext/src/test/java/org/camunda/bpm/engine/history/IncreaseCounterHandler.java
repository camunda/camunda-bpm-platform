package org.camunda.bpm.engine.history;

import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * Just for Testing.
 *
 * @author  jbellmann
 */
public class IncreaseCounterHandler implements HistoryEventHandler {

    private final AtomicInteger counter;

    public IncreaseCounterHandler(final AtomicInteger counter) {
        this.counter = counter;
    }

    @Override
    public void handleEvent(final HistoryEvent historyEvent) {
        counter.incrementAndGet();
    }

}
