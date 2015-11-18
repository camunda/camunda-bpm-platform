package org.camunda.bpm.engine.rest.jersey;

import org.camunda.bpm.engine.rest.AbstractIncidentRestServiceQueryTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.JerseyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class IncidentRestServiceQueryTest extends AbstractIncidentRestServiceQueryTest {

  protected static EmbeddedServerBootstrap serverBootstrap;

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new JerseyServerBootstrap();
    serverBootstrap.start();
  }

  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }

}
