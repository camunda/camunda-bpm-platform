package com.camunda.fox.platform.test.deployment.war;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.Assert;

import org.activiti.engine.impl.util.IoUtil;
import org.camunda.bpm.application.impl.deployment.metadata.ProcessesXmlParser;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.client.impl.ProcessArchiveImpl;
import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.DeploymentHelper;
import com.camunda.fox.platform.test.util.JndiConstants;

/**
 * In this test we make sure that if a user deploys a WAR file with a broken
 * .bpmn-XML file, the deployment fails.
 * 
 * @author Daniel Meyer
 * 
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithBrokenBpmnXml_internal extends AbstractFoxPlatformIntegrationTest {

  
  @Deployment
  public static WebArchive processArchive() {    
    
    return  ShrinkWrap.create(WebArchive.class, "test.war")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(JndiConstants.class)
      .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
      .addAsResource("com/camunda/fox/platform/test/deployment/war/TestWarDeploymentWithBrokenBpmnXml.testXmlInvalid.bpmn20.xml");           
  }
  
  @Inject
  private ProcessArchiveSupport processApplication;
  
  @Test
  public void testXmlInvalid() {
    
    ProcessArchiveXml processArchiveXml = new ProcessArchiveXml();
    processArchiveXml.name = "test";
    
    
    final String resourceName = "com/camunda/fox/platform/test/deployment/war/TestWarDeploymentWithBrokenBpmnXml.testXmlInvalid.bpmn20.xml";
    InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceName);
    final byte[] process = IoUtil.readInputStream(resourceAsStream, resourceName);
    IoUtil.closeSilently(resourceAsStream);

    ProcessArchiveImpl pa = new ProcessArchiveImpl(processArchiveXml, null, processApplication.getReference(), processApplication) {
      public Map<String, byte[]> getProcessResources() {
        HashMap<String, byte[]> map = new HashMap<String, byte[]>();
        map.put(resourceName, process);
        return map;
      }
      public Map<String, Object> getProperties() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(ProcessesXmlParser.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, false);
        map.put(ProcessesXmlParser.PROP_IS_DELETE_UPON_UNDEPLOY, true);
        return map;
      }
    };    
    
    try {
      processArchiveService.installProcessArchive(pa);
      Assert.fail("exception expected!");
    }catch(Exception e) {
      // expected
    }
    
  
  }

}
