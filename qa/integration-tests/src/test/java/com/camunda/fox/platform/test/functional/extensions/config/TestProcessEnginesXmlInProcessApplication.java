package com.camunda.fox.platform.test.functional.extensions.config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEnginesXmlInProcessApplication extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    return initWebArchiveDeployment()
            .addAsWebInfResource("singleEngine.xml", "classes/META-INF/process-engines.xml");
  }
  
  @Test
  public void testDeployProcessArchive() {
   Assert.assertNotNull(processEngineService.getProcessEngine("engine1"));
  }

}
