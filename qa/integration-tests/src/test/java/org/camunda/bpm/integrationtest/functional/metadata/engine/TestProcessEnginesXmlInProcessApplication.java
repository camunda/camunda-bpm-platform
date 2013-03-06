package org.camunda.bpm.integrationtest.functional.metadata.engine;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEnginesXmlInProcessApplication extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
      .addAsResource("singleEngine.xml", "META-INF/processes.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class);
  }
  
  @Test
  public void testDeployProcessArchive() {
   Assert.assertNotNull(processEngineService.getProcessEngine("engine1"));
  }

}
