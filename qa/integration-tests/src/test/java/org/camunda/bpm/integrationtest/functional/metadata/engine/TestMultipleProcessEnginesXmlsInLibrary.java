package org.camunda.bpm.integrationtest.functional.metadata.engine;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This time, we have two process-engines.xml files in separate library jars.
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestMultipleProcessEnginesXmlsInLibrary extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    return initWebArchiveDeployment()
            .addAsLibraries(
              ShrinkWrap.create(JavaArchive.class, "engine1.jar")
                    .addAsResource("singleEngine.xml", "META-INF/processes.xml"),
              ShrinkWrap.create(JavaArchive.class, "engine2.jar")
                   .addAsResource("twoEngines.xml", "META-INF/processes.xml")
         );
  }
  
  @Test
  public void testDeployProcessArchive() {
    Assert.assertNotNull(processEngineService.getProcessEngine("engine1"));
    Assert.assertNotNull(processEngineService.getProcessEngine("engine2"));
    Assert.assertNotNull(processEngineService.getProcessEngine("engine3"));
  }

}
