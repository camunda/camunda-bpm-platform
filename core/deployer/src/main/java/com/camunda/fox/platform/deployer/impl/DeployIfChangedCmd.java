/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.camunda.fox.platform.deployer.impl;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Command creating a deployment if at least one process has changed
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class DeployIfChangedCmd implements Command<String>, Serializable {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(DeployIfChangedCmd.class.getName());
  
  protected Map<String, byte[]> resources;
  protected ClassLoader classloader;
  protected final String name;
  protected transient CommandContext commandContext;
  protected final HashSet<String> activeDeployments;

  public DeployIfChangedCmd(String name, Map<String, byte[]> resources, ClassLoader classloader, HashSet<String> activeDeployments) {
    this.resources = resources;
    this.classloader = classloader;
    this.name = name;
    this.activeDeployments = activeDeployments;
  }

  public String execute(CommandContext commandContext) {
    this.commandContext = commandContext;

    String deploymentId = null;
    try {
      if (name == null) {
        throw new ActivitiException("Deployment 'name' cannot be null.");
      }
      if (resources.isEmpty()) {
        throw new ActivitiException("Deployment must contain at least one resource.");
      }
      
      deploymentId = tryResume();
      if (deploymentId != null) {
        log.info("No changes in  '" + name + "', loading exisiting process engine deployment with id " + deploymentId);
      } else {
        deploymentId = createNewDeployment();
        log.info("Created new process engine deployment with id " + deploymentId);
      }

      return deploymentId;

    } catch (Exception e) {
      throw new ActivitiException("Could not deploy '" + name + "': " + e.getMessage(), e);
    }
  }

  protected String createNewDeployment() {
    // create a new deployment
    DeploymentEntity deployment = new DeploymentEntity();
    deployment.setName(name);
    deployment.setDeploymentTime(new Date());
    deployment.setNew(true);

    for (Entry<String, byte[]> resourceEntry : resources.entrySet()) {
      ResourceEntity resource = new ResourceEntity();
      resource.setName(resourceEntry.getKey());
      resource.setBytes(resourceEntry.getValue());
      deployment.addResource(resource);
    }

    // perform the deployment
    commandContext.getDeploymentManager().insertDeployment(deployment);

    return deployment.getId();
  }

  /**
   * iterate all processes: check whether there already exists a process definition with the same key/id if true and
   * belongs to a deployment with a different name as the current deployment, throw exception if it is part of a
   * deployment with the same name: check whether it has changed if true deploy a new version ( i.e. the method returns
   * null )
   * @throws Exception
   */
  protected String tryResume() throws Exception {
    // try to retrieve a deployment with the same name:
    ProcessDefinition definition = null;
    String deploymentId = null;
    boolean deployProcessArchive = false;
    
    for (Entry<String, byte[]> resourceEntry : resources.entrySet()) {
      String resourceName = resourceEntry.getKey();
      boolean isBpmnResource = isBpmnResource(resourceName);
      if (isBpmnResource) { // ignore non-BPMN files
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(resourceEntry.getValue()));
          
        NodeList definitionElements = doc.getElementsByTagNameNS("*", "process");
        for (int i = 0; i < definitionElements.getLength(); i++) {
          Element definitionElement = (Element) definitionElements.item(i);
          String isExecutableStr = definitionElement.getAttribute("isExecutable");
          if (isExecutableStr.isEmpty() || Boolean.parseBoolean(isExecutableStr)) {
            String processDefinitionKey = definitionElement.getAttribute("id");
            if (processDefinitionKey != null) {
              // get latest definition for the same key (if exists): 
              definition = new ProcessDefinitionQueryImpl(commandContext).processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
            }
            if (definition == null) {
              // 'resourceEntry' is a new process, not contained in the last deployment. 
              // We need to create a new deployment.
              deployProcessArchive = true;
            } else if (definition.getDeploymentId() == null) {
              deployProcessArchive = true;
            } else {
              // candidate for deployment:
              Deployment deployment = new DeploymentQueryImpl(commandContext).deploymentId(definition.getDeploymentId()).singleResult();
      
              if (deployment.getName().equals(name)) {
                // check whether the process has changed:
                if (hasChanged(resourceEntry, deployment)) {
                  log.info("The process '" + resourceName + "' has changed, redeploying.");
                  deployProcessArchive = true;
                } else {
                  deploymentId = deployment.getId();
                }
              } else if (!activeDeployments.contains(deployment.getId())) {
                // a process with the same key was deployed in a processArchive with a 
                // different name, but that processArchive is not currently active (i.e. was undeployed).
                deployProcessArchive = true;
              } else {
                throw new ActivitiException("Cannot deploy process with id/key='" + definition.getKey()
                  + "', a process with the same key is already deployed in process-archive '" + deployment.getName() + "' (deployment id='" + deployment.getId()
                  + "') and " + deployment.getName() + " is active. Undeploy the existing process-archive or change the key of the process.");
              }
            }
            
            if (deployProcessArchive) {
              return null;
            }
          } else {
            log.info("Ignoring non-executable process with id='" + definitionElement.getAttribute("id") + "'. Set the attribute isExecutable=\"true\" to deploy this process.");
          }
        }
      }
    }
    
    if (!deployProcessArchive && deploymentId == null) {
      // all processes are flagged isExecutable = false
      log.info("No process marked for execution. Set at least one process to isExecutable=\"true\".");
    }
    
    return deploymentId;
  }

  private boolean isBpmnResource(String resourceName) {
    boolean isBpmnResource = false;
    for (String bpmnResourceSuffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(bpmnResourceSuffix)) {
        isBpmnResource = true;
        break;
      }
    }
    return isBpmnResource;
  }

  /**
   * TODO: Check existing DeployCmd.deploymentsDiffer method (property duplicateFilterEnabled) which should be able to
   * to the same (on a byte array level but for all resources).
   *
   * Would be nice to have the same behaviour everywhere (especially in OSGI and Java EE)
   */
  protected boolean hasChanged(Entry<String, byte[]> resourceEntry, Deployment deployment) {
    
    if(resourceEntry == null || resourceEntry.getKey() == null) {
      return true;
    }
    
    String deploymentId = deployment.getId();
    String resourceName = resourceEntry.getKey();
    
    DeploymentEntity deploymentEntity = commandContext.getDeploymentManager().findDeploymentById(deploymentId);
    if(deploymentEntity == null || deploymentEntity.getResource(resourceName) == null) {
      return true;
    }
    
    ResourceEntity resourceEntity = deploymentEntity.getResource(resourceName);
    byte[] existingProcessDefinitionBytes = resourceEntity.getBytes();
    
    if(existingProcessDefinitionBytes == null) {
      return true;
    }

    try {
      String existingProcessDefinitionXml = new String(existingProcessDefinitionBytes, "UTF8");
      String newProcessDefinitionXml = new String(resourceEntry.getValue(), "UTF8");
      boolean returnVal = !xmlEquals(existingProcessDefinitionXml, newProcessDefinitionXml);
      return returnVal;
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not compare process definitions", e);
      return true;
    }
  }

  /**
   * (copied verbatim from Jbpm deployer)
   */
  protected boolean xmlEquals(String xml1, String xml2) {
    return removeIgnoredCharacters(xml1).equals(
      removeIgnoredCharacters(xml2));
  }

  /**
   * (copied verbatim from Jbpm deployer)
   *
   * replace characters in xml which should be ignored while comparing two processdefinition.xml's. Basically these are
   * new lines and tabs.
   *
   * White spaces are dangerous, maybe only a white space in a attribute name has changed!
   *
   * TODO: add more sophisticated way of comparing?
   */
  protected String removeIgnoredCharacters(String st) {
    return st.replaceAll("\n", "").replaceAll("\t", "");
  }

}
