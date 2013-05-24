package org.camunda.bpm.engine.rest.resteasy;

import org.camunda.bpm.engine.rest.AbstractManagementRestServiceTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ManagementRestServiceTest extends AbstractManagementRestServiceTest {

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
	
}
