package org.camunda.bpm.engine.rest.wink;

import org.camunda.bpm.engine.rest.AbstractExceptionHandlerTest;
import org.camunda.bpm.engine.rest.util.WinkTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

public class ExceptionHandlerTest extends AbstractExceptionHandlerTest {

  private static final String CUSTOM_RESOURCE_APPLICATION_WEB_XML_PATH = "runtime/wink/custom-application-web.xml";

  protected static WinkTomcatServerBootstrap serverBootstrap;

  @ClassRule
  public static TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new WinkTomcatServerBootstrap(CUSTOM_RESOURCE_APPLICATION_WEB_XML_PATH);
    serverBootstrap.setWorkingDir(temporaryFolder.getRoot().getAbsolutePath());
    serverBootstrap.start();
  }
  
  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }
}
