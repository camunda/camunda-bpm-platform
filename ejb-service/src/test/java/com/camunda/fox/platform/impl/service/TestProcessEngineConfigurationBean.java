package com.camunda.fox.platform.impl.service;

import java.util.concurrent.ExecutionException;

import javax.ejb.EJB;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.test.H2Datasource;

@RunWith(Arquillian.class)
public class TestProcessEngineConfigurationBean {

  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(H2Datasource.class)
            .addClass(PlatformServiceBean.class)
            .addClass(ProcessEngineConfigurationBean.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml");
  }
  
  @EJB
  private ProcessEngineService processEngineService;
    
  @Test
  public void testProcessEngineStarted() throws NamingException, InterruptedException, ExecutionException {
   
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
      
  }

}
