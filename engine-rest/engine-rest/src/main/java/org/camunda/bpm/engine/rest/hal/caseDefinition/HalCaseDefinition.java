/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal.caseDefinition;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.rest.CaseDefinitionRestService;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.hal.HalIdResource;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourcesResource;
import org.camunda.bpm.engine.rest.util.ApplicationContextPathUtil;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class HalCaseDefinition extends HalResource<HalCaseDefinition> implements HalIdResource {
  public static final HalRelation REL_SELF =
    HalRelation.build("self", CaseDefinitionRestService.class, UriBuilder.fromPath(CaseDefinitionRestService.PATH).path("{id}"));
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
  protected String name;
  protected int version;
  protected String resource;
  protected String deploymentId;
  protected String contextPath;

  public static HalCaseDefinition fromCaseDefinition(CaseDefinition caseDefinition, ProcessEngine processEngine) {
    HalCaseDefinition halCaseDefinition = new HalCaseDefinition();

    halCaseDefinition.id = caseDefinition.getId();
    halCaseDefinition.key = caseDefinition.getKey();
    halCaseDefinition.category = caseDefinition.getCategory();
    halCaseDefinition.name = caseDefinition.getName();
    halCaseDefinition.version = caseDefinition.getVersion();
    halCaseDefinition.resource = caseDefinition.getResourceName();
    halCaseDefinition.deploymentId = caseDefinition.getDeploymentId();
    halCaseDefinition.contextPath = ApplicationContextPathUtil.getApplicationPathForDeployment(processEngine, caseDefinition.getDeploymentId());

    halCaseDefinition.linker.createLink(REL_SELF, caseDefinition.getId());
    halCaseDefinition.linker.createLink(REL_DEPLOYMENT, caseDefinition.getDeploymentId());
    halCaseDefinition.linker.createLink(REL_DEPLOYMENT_RESOURCE, caseDefinition.getDeploymentId(), caseDefinition.getResourceName());

    return halCaseDefinition;
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

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getResource() {
    return resource;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getContextPath() {
    return contextPath;
  }

}
