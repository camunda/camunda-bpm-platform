/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.deployer.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;


/**
 * 
 * @author Daniel Meyer
 *
 */
public class TestFoxDeployer extends FoxDeployerTestcase {

  public void testDeployEmptyArchive() {    
    try {
      deployer.deploy("processArchive", new HashMap<String, byte[]>(), getClass().getClassLoader());
      fail("exception expected");
    }catch (Exception e) {
      if(!e.getCause().getMessage().contains("Deployment must contain at least one resource")) {
        fail("wrong exception");  
      }
      // expected
    }
  }
  
  public void testDeployNoNameArchive() {    
    try {
      deployer.deploy(null, new HashMap<String, byte[]>(), getClass().getClassLoader());
      fail("exception expected");
    }catch (Exception e) {     
      // expected
    }
  }
  
  public void testDeployUndeployDeleteTrue() {
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
    
    deployer.deploy(name, resources, cl);
    
    assertNotNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNotNull(repositoryService.createProcessDefinitionQuery().singleResult());
    assertNotNull(repositoryService.createProcessDefinitionQuery().active().singleResult());
    
    deployer.unDeploy(name, true);
    
    assertNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().singleResult());
  }
  
  
  public void testDeployUndeployDeleteFalse() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
    
    deployer.deploy(name, resources, cl);
    
    assertNotNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNotNull(repositoryService.createProcessDefinitionQuery().singleResult());
    assertNotNull(repositoryService.createProcessDefinitionQuery().active().singleResult());
    
    deployer.unDeploy(name, false);
    
    assertNotNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNotNull(repositoryService.createProcessDefinitionQuery().singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().active().singleResult());    // process definition is suspended
    assertNotNull(repositoryService.createProcessDefinitionQuery().suspended().singleResult());
    
    // cleanup
    deployer.unDeploy(name, true);    
  }
  
  public void testDeploySameKeyFails() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
    
    deployer.deploy(name, resources, cl);
    try {
      deployer.deploy("differentName", resources, cl);
      fail("exception expected");
    }catch (Exception e) {
      assertTrue(e.getMessage().contains("Could not deploy 'differentName': Cannot deploy process with id/key='oneTaskProcess', a process with the same key is already deployed"));
    }
    
    // cleanup
    deployer.unDeploy(name, true);      
  }
  
  public void testScenarioRedeployDifferentName() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
        
    deployer.deploy(name, resources, cl);
    
    deployer.unDeploy(name, false);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // re-deploy same version, differnet name of the process archive
    deployer.deploy("newName", resources, cl);
    
    assertEquals(2, repositoryService.createDeploymentQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    // old processes are activated
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    // clean db:
    deployer.unDeploy(name, true);    
    deployer.unDeploy("newName", true);    
  }
  
  public void testScenarioRedeployNoChangesInProcess() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
        
    deployer.deploy(name, resources, cl);
    
    deployer.unDeploy(name, false);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // re-deploy same version
    deployer.deploy(name, resources, cl);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    
    // clean db:
    deployer.unDeploy(name, true);    
    
  }
  
  public void testScenarioRedeployNoChangesInProcessForUTF8() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("fox-invoice.bpmn");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("fox-invoice.bpmn", process.getBytes());
        
    String deploymentId = deployer.deploy(name, resources, cl);
    
    assertNotNull(deploymentId);
    
    deployer.unDeploy(name, false);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // re-deploy same version
    String noChangesProcess = IoUtil.readFileAsString("fox-invoice.bpmn");
    resources = new HashMap<String, byte[]>();
    resources.put("fox-invoice.bpmn", noChangesProcess.getBytes());
    
    deployer.deploy(name, resources, cl);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    
    // clean db:
    deployer.unDeploy(name, true);    
    
  }
  
  public void testScenarioReDeployAdonisProcess() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    deployer.unDeploy(name, true);
    
    String process = IoUtil.readFileAsString("adonis-invoice.bpmn");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("adonis-invoice.bpmn", process.getBytes());
        
    String deploymentId = deployer.deploy(name, resources, cl);
    
    assertNotNull(deploymentId);
    
    deployer.unDeploy(name, false);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // re-deploy same version
    String noChangesProcess = IoUtil.readFileAsString("adonis-invoice.bpmn");
    resources = new HashMap<String, byte[]>();
    resources.put("adonis-invoice.bpmn", noChangesProcess.getBytes());
    
    deployer.deploy(name, resources, cl);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    
    // clean db:
    deployer.unDeploy(name, true);    
    
  }

  
  public void testScenarioUpgrade() {    
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("oneTaskProcess.bpmn20.xml");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("oneTaskProcess.bpmn20.xml", process.getBytes());
    
    String processUpgrade = IoUtil.readFileAsString("oneTaskProcessUpgrade.bpmn20.xml");
    HashMap<String, byte[]> resourcesUpgrade = new HashMap<String, byte[]>();
    resourcesUpgrade.put("oneTaskProcess.bpmn20.xml", processUpgrade.getBytes());
    
    deployer.deploy(name, resources, cl);
    
    deployer.unDeploy(name, false);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // deploy a new version:
    deployer.deploy(name, resourcesUpgrade, cl);
    
    assertEquals(2, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    // deploying the new version activates previous versions
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    // clean db:
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);      
    }
    
  }
  
  public void testDeployNonBpmnFile() {
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";

    System.err.println();
    String process = IoUtil.readFileAsString("invoice.png");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("invoice.png", process.getBytes());

    // capture messages in System.err from the XML parser in DeployIfChangedCmd#getExistingProcessDefinition(String, byte[])
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));

    deployer.deploy(name, resources, cl);

    assertEquals("System.err is not empty.", "", errContent.toString());
    System.setErr(null); // reset System.err

    assertNotNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().active().singleResult());

    deployer.unDeploy(name, true);

    assertNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().singleResult());
  }

  public void testDeployOnlyIsExecutableFlaggedProcesses() {
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("collaboration_with_non_executable_process.bpmn");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("collaboration_with_non_executable_process.bpmn", process.getBytes());
    
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));

    String deploymentId = deployer.deploy(name, resources, cl);
    
    assertEquals("System.err is not empty.", "", errContent.toString());
    System.setErr(null); // reset System.err
    
    assertNotNull(deploymentId);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    
    deployer.unDeploy(name, true);
    
    assertNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().singleResult());
  }
  
  public void testDontDeployIsExecutableFalseFlaggedProcesses() {
    ClassLoader cl = getClass().getClassLoader();
    String name = "processArchive";
    
    String process = IoUtil.readFileAsString("collaboration_with_non_executable_processes.bpmn");
    HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("collaboration_with_non_executable_processes.bpmn", process.getBytes());
    
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));

    String deploymentId = deployer.deploy(name, resources, cl);
    
    assertEquals("System.err is not empty.", "", errContent.toString());
    System.setErr(null); // reset System.err
    
    assertNotNull(deploymentId);
    
    assertEquals(1, repositoryService.createDeploymentQuery().deploymentName(name).count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    
    deployer.unDeploy(name, true);
    
    assertNull(repositoryService.createDeploymentQuery().deploymentName(name).singleResult());
    assertNull(repositoryService.createProcessDefinitionQuery().singleResult());
  }
}
