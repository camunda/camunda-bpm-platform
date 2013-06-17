package org.camunda.bpm.engine.history;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author  jbellmann
 */
public class HistoryEventHandlerCompositeTest {

    private AtomicInteger counter = new AtomicInteger(0);

    @Before
    public void setUp() {
        counter = new AtomicInteger(0);
    }

    @Test
    public void handleEventsWithOneHandler() {

        assertMultipleHandlers(1);
    }

    @Test
    public void handleEventsWithTwoHandlers() {

        assertMultipleHandlers(2);
    }

    @Test
    public void handleEventsWithThreeHandlers() {

        assertMultipleHandlers(3);
    }

    protected void assertMultipleHandlers(final int n) {
        HistoryEventHandlerComposite handlerComposite = new HistoryEventHandlerComposite(getHandlerList(n));

        HistoryEvent event = new HistoryEvent();
        handlerComposite.handleEvent(event);

        Assert.assertEquals("Counter should be at " + n, n, counter.get());
    }

    protected List<HistoryEventHandler> getHandlerList(final int n) {
        List<HistoryEventHandler> result = Lists.newArrayList();
        for (int i = 0; i < n; i++) {
            result.add(new IncreaseCounterHandler(counter));
        }

        return result;
    }

}
