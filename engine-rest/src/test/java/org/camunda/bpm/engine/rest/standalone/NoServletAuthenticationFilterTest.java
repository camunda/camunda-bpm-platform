package org.camunda.bpm.engine.rest.standalone;

import org.camunda.bpm.engine.rest.util.ResteasyTomcatServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

public class NoServletAuthenticationFilterTest extends AbstractAuthenticationFilterTest {

  protected static ResteasyTomcatServerBootstrap serverBootstrap;

  @ClassRule
  public static TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new ResteasyTomcatServerBootstrap("runtime/resteasy/auth-filter-no-servlet-web.xml");
    serverBootstrap.setWorkingDir(temporaryFolder.getRoot().getAbsolutePath());
    serverBootstrap.start();
  }

  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }

}
