package org.camunda.bpm.engine.rest.history.wink;

import org.camunda.bpm.engine.rest.history.AbstractHistoricVariableInstanceRestServiceQueryTest;
import org.camunda.bpm.engine.rest.util.WinkTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class HistoricVariableInstanceRestServiceQueryTest extends AbstractHistoricVariableInstanceRestServiceQueryTest {

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
