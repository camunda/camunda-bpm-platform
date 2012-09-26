package com.camunda.fox.cycle.web.service.resource;

import static org.junit.Assert.assertEquals;

import com.camunda.fox.cycle.connector.vfs.VfsConnector;

public class VfsRoundtripServiceTest extends RoundtripServiceTest {

  @Override
  protected void initConnector() {
    setConnector(getConnectorRegistry().getConnector(1));
    assertEquals(VfsConnector.class.getName(), getConnector().getConfiguration().getConnectorClass());
  }

}
