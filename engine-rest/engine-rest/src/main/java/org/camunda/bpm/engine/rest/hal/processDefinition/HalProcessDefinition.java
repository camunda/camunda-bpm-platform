/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal.processDefinition;

import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.hal.HalIdResource;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourcesResource;
import org.camunda.bpm.engine.rest.util.ApplicationContextPathUtil;

/**
 * @author Daniel Meyer
 *
 */
public class HalProcessDefinition extends HalResource<HalProcessDefinition> implements HalIdResource {

  public static final HalRelation REL_SELF =
    HalRelation.build("self", ProcessDefinitionRestService.class, UriBuilder.fromPath(ProcessDefinitionRestService.PATH).path("{id}"));
  public static final HalRelation REL_DEPLOYMENT =
    HalRelation.build("deployment", DeploymentRestService.class, UriBuilder.fromPath(DeploymentRestService.PATH).path("{id}"));
  public static final HalRelation REL_DEPLOYMENT_RESOURCE = HalRelation.build("resource", DeploymentResourcesResource.class,
      UriBuilder.fromPath(DeploymentRestService.PATH)
                .path("{deploymentId}")
                .path("resources")
                .path("{resourceId}"));

  protected String id;
  protected String key;
  protected String category;
  protected String description;
  protected String name;
  protected String versionTag;
  protected int version;
  protected String resource;
  protected String deploymentId;
  protected String diagram;
  protected boolean suspended;
  protected String contextPath;

  public static HalProcessDefinition fromProcessDefinition(ProcessDefinition processDefinition, ProcessEngine processEngine) {
    HalProcessDefinition halProcDef = new HalProcessDefinition();

    halProcDef.id = processDefinition.getId();
    halProcDef.key = processDefinition.getKey();
    halProcDef.category = processDefinition.getCategory();
    halProcDef.description = processDefinition.getDescription();
    halProcDef.name = processDefinition.getName();
    halProcDef.version = processDefinition.getVersion();
    halProcDef.versionTag = processDefinition.getVersionTag();
    halProcDef.resource = processDefinition.getResourceName();
    halProcDef.deploymentId = processDefinition.getDeploymentId();
    halProcDef.diagram = processDefinition.getDiagramResourceName();
    halProcDef.suspended = processDefinition.isSuspended();
    halProcDef.contextPath = ApplicationContextPathUtil.getApplicationPathForDeployment(processEngine, processDefinition.getDeploymentId());

    halProcDef.linker.createLink(REL_SELF, processDefinition.getId());
    halProcDef.linker.createLink(REL_DEPLOYMENT, processDefinition.getDeploymentId());
    halProcDef.linker.createLink(REL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName());

    return halProcDef;
  }

  public String getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public String getResource() {
    return resource;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getDiagram() {
    return diagram;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getContextPath() {
    return contextPath;
  }

}
