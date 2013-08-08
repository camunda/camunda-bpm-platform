package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.history.HistoryEventHandlerComposite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Simple Test for configuring the eventHandlerComposite.
 * 
 * @author jbellmann
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/compositeXmlConfigurationTest.xml")
public class CompositeXmlConfigurationTest {

  @Autowired
  private IncreaseCounterHandler counter;

  @Autowired
  private HistoryEventHandlerComposite composite;

  @Test
  public void initialized() {
    Assert.assertNotNull(composite);
    composite.handleEvent(EventBuilder.buildHistoryEvent());
    Assert.assertTrue(counter.getCount() == 1);
  }
}
