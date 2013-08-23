package org.camunda.bpm.engine.rest.standalone;

import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.ResteasyTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ServletAuthenticationFilterTest extends AbstractAuthenticationFilterTest {

  protected static EmbeddedServerBootstrap serverBootstrap;

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new ResteasyTomcatServerBootstrap("runtime/resteasy/auth-filter-servlet-web.xml");
    serverBootstrap.start();
  }

  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }

}
