package org.camunda.bpm.engine.rest.history.wink;

import org.camunda.bpm.engine.rest.history.AbstractHistoricActivityInstanceRestServiceQueryTest;
import org.camunda.bpm.engine.rest.util.WinkTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class HistoricActivityInstanceRestServiceQueryTest extends AbstractHistoricActivityInstanceRestServiceQueryTest {

  protected static WinkTomcatServerBootstrap serverBootstrap;

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new WinkTomcatServerBootstrap();
    serverBootstrap.start();
  }

  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }
}
