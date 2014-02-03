package org.camunda.bpm.engine.rest.history.jersey;

import org.camunda.bpm.engine.rest.history.AbstractHistoricActivityStatisticsRestServiceQueryTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.JerseyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class HistoricActivityStatisticsRestServiceQueryTest extends AbstractHistoricActivityStatisticsRestServiceQueryTest {

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
