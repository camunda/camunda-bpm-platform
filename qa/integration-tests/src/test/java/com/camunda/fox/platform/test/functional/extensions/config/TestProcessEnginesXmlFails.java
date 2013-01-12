package com.camunda.fox.platform.test.functional.extensions.config;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.DeploymentHelper;

/**
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEnginesXmlFails {

  @ArquillianResource
  private Deployer deployer;
  
  @Deployment(managed=false, name="deployment")
  public static WebArchive processArchive() {    
    
    return  ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
            .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
            .addAsLibraries(
              ShrinkWrap.create(JavaArchive.class, "engine1.jar")
                    .addAsResource("singleEngine.xml", "META-INF/process-engines.xml"),
              ShrinkWrap.create(JavaArchive.class, "engine2.jar")
                    // we add the same process engine configuration multiple times -> fails
                   .addAsResource("singleEngine.xml", "META-INF/process-engines.xml")
         );
  }
  
  @Test
  @RunAsClient
  public void testDeployProcessArchive() {
    try {
      deployer.deploy("deployment");
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
  }

}
