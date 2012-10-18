package com.camunda.fox.cycle.web.service.resource;

import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.vfs.VfsConnector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class VfsRoundtripServiceTest extends RoundtripServiceTest {
  
  private static Class<? extends Connector> CONNECTOR_CLS = VfsConnector.class;
  
  @Override
  protected void initConnector() {
    setConnector(getConnectorRegistry().getConnector(CONNECTOR_CLS));
    assertEquals(CONNECTOR_CLS.getName(), getConnector().getConfiguration().getConnectorClass());
  }
}
