package org.camunda.bpm.integrationtest.functional.drools;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * In this test we make sure that if a user deploys a WAR file with a broken
 * .bpmn-XML file, the deployment fails.
 * 
 * @author Daniel Meyer
 * 
 */
@RunWith(Arquillian.class)
public class TestDeploymentWithDroolsTaskFails {

  @ArquillianResource
  private Deployer deployer;
  
  @Deployment(managed=false, name="deployment")
  public static Archive<?> processArchive() {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addAsLibraries(DeploymentHelper.getEjbClient())
      .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/drools/TestDeploymentWithDroolsTaskFails.testDeployDroolsFails.bpmn20.xml");

    return AbstractFoxPlatformIntegrationTest.processArchiveDeployment(archive);
  }
  
  @Test
  @RunAsClient
  public void testDeployDroolsFails() {
    try {
      deployer.deploy("deployment");
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
  }

}
