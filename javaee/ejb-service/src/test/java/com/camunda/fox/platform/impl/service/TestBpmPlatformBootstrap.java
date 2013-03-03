package com.camunda.fox.platform.impl.service;

import java.util.concurrent.ExecutionException;

import javax.naming.NamingException;

import org.camunda.bpm.BpmPlatform;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.impl.service.deployment.EjbJarAttachments;
import com.camunda.fox.platform.impl.service.deployment.EjbJarParsePlatformXmlStep;
import com.camunda.fox.platform.impl.test.H2Datasource;

/**
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class TestBpmPlatformBootstrap {

  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addClass(H2Datasource.class)
      .addClass(BpmPlatformBootstrapBean.class)
      .addClass(EjbJarAttachments.class)
      .addClass(EjbJarParsePlatformXmlStep.class)
      .addAsWebInfResource("META-INF/bpm-platform.xml", "classes/META-INF/bppm-platform.xml")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");         
  }
  
    
  @Test
  public void testDefaultProcessEngineAvailable() throws NamingException, InterruptedException, ExecutionException {
    
    Assert.assertNotNull(BpmPlatform.getDefaultProcessEngine());
   
  }
    
  
}
