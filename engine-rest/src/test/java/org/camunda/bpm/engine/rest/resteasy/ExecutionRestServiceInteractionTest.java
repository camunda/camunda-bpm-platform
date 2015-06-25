package org.camunda.bpm.engine.rest.resteasy;

import org.camunda.bpm.engine.rest.AbstractExecutionRestServiceInteractionTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ExecutionRestServiceInteractionTest extends AbstractExecutionRestServiceInteractionTest {

  protected static EmbeddedServerBootstrap serverBootstrap;

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    serverBootstrap = new ResteasyServerBootstrap();
    serverBootstrap.start();
  }

  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    serverBootstrap.stop();
  }

  @Test
  @Ignore("Ignored until REST assured issue #413 is closed and our version is upgraded accordingly")
  @Override
  public void testGetFileVariableDownloadWithTypeAndEncoding() {
    super.testGetFileVariableDownloadWithTypeAndEncoding();
  }

}
