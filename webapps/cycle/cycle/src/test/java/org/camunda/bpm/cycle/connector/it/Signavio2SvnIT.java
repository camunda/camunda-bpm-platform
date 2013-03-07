package org.camunda.bpm.cycle.connector.it;

import org.camunda.bpm.cycle.connector.Connector;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Tests Signavio to Svn integration.
 * 
 * @author christian.lipphardt@camunda.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test/connector-it-configuration.xml",
          "classpath:/spring/test-context.xml", "classpath:/spring/test-persistence.xml"}
)
public class Signavio2SvnIT extends AbstractConnectorInteractionITBase {
  
  @Override
  public Connector getLhsConnector() {
    return getSignavioConnector();
  }

  @Override
  public Connector getRhsConnector() {
    return getSvnConnector();
  }

}
