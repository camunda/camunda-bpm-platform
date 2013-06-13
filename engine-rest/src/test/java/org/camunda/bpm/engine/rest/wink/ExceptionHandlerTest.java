package org.camunda.bpm.engine.rest.wink;

import org.camunda.bpm.engine.rest.AbstractExceptionHandlerTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.WinkTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ExceptionHandlerTest extends AbstractExceptionHandlerTest {

  private static final String CUSTOM_RESOURCE_APPLICATION_WEB_XML_PATH = "runtime/wink/custom-application-web.xml";
  
  protected static EmbeddedServerBootstrap serverBootstrap;  
  
  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new WinkTomcatServerBootstrap(CUSTOM_RESOURCE_APPLICATION_WEB_XML_PATH);
    serverBootstrap.start();
  }
  
  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }
}
