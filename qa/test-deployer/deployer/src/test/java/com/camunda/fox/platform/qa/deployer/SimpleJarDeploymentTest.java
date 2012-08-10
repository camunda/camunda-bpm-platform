package com.camunda.fox.platform.qa.deployer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

import com.camunda.fox.cdi.TestProcessEngineLookup;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@RunWith(Arquillian.class)
public class SimpleJarDeploymentTest extends AbstractSimpleDeploymentTestBase {
    
  @Deployment
  public static Archive<?> createDeplomentArchive() {
    return ShrinkWrap
      .create(JavaArchive.class)
        .addClass(TestDelegate.class)
        .addClass(TestCdiBean.class)
        .addClass(TestProcessEngineLookup.class)
        .addPackages(true, "org.fest")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }
}
