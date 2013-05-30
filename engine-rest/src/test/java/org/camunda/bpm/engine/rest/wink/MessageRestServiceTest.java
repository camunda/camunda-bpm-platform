package org.camunda.bpm.engine.rest.wink;

import org.camunda.bpm.engine.rest.AbstractMessageRestServiceTest;
import org.camunda.bpm.engine.rest.util.WinkTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class MessageRestServiceTest extends AbstractMessageRestServiceTest {

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
