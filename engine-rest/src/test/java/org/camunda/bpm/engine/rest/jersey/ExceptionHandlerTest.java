package org.camunda.bpm.engine.rest.jersey;

import org.camunda.bpm.engine.rest.AbstractExceptionHandlerTest;
import org.camunda.bpm.engine.rest.application.TestCustomResourceApplication;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.JerseyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ExceptionHandlerTest extends AbstractExceptionHandlerTest {

  protected static EmbeddedServerBootstrap serverBootstrap;  
  
  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new JerseyServerBootstrap(new TestCustomResourceApplication());
    serverBootstrap.start();
  }
  
  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }
}
