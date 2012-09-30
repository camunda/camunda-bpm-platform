package com.camunda.fox.cycle.web.service.resource;

import static org.junit.Assert.assertEquals;

import com.camunda.fox.cycle.connector.svn.SvnConnector;


public class SvnRoundtripServiceTest extends RoundtripServiceTest {

  @Override
  protected void initConnector() {
    setConnector(getConnectorRegistry().getConnector(3));
    assertEquals(SvnConnector.class.getName(), getConnector().getConfiguration().getConnectorClass());
  }

}
