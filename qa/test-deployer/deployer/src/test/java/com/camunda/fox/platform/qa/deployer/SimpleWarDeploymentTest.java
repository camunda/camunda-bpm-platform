package com.camunda.fox.platform.qa.deployer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import com.camunda.fox.cdi.TestProcessEngineLookup;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@RunWith(Arquillian.class)
public class SimpleWarDeploymentTest extends AbstractSimpleDeploymentTestBase {
    
  @Deployment
  public static Archive<?> createDeplomentArchive() {
    WebArchive archive = ShrinkWrap
                    .create(WebArchive.class)
                      .addClass(TestProcessEngineLookup.class)
                      .addClass(TestCdiBean.class)
                      .addClass(TestDelegate.class)
                      .addPackages(true, "org.fest")
                      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    
    return archive;
  }
}
