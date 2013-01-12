package com.camunda.fox.platform.test.functional.extensions.config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * This time, the process-engines.xml file is located in a library jar.
 * 
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEnginesXmlInLibrary extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    return initWebArchiveDeployment()
            .addAsLibraries(ShrinkWrap.create(JavaArchive.class, "engine1.jar")
                    .addAsResource("singleEngine.xml", "META-INF/process-engines.xml")
             );
            
  }
  
  @Test
  public void testDeployProcessArchive() {
   Assert.assertNotNull(processEngineService.getProcessEngine("engine1"));
  }

}
