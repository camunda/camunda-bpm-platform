package com.camunda.fox.platform.qa.deployer.test;

import junit.framework.Assert;
import org.fest.assertions.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@RunWith(Arquillian.class)
public class DeploymentWithFestAssertTest {
  
  @Deployment
  public static Archive<?> createDeplomentArchive() {
    return ShrinkWrap.create(JavaArchive.class)
              .addClass(ExampleBean.class)
              .addPackages(true, "org.fest")
              .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml");
  }
  
  @Test
  public void shouldHaveFestAssertionsOnClassPath() throws Exception {
    Class.forName("org.fest.assertions.Assertions");
  }
  
  @Test
  public void shouldLoadTest() {
    Assertions.assertThat(true).isTrue();
  }
}
