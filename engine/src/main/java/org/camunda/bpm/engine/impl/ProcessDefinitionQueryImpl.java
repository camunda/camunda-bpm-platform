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

package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinitionQuery, ProcessDefinition>
  implements ProcessDefinitionQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected boolean latest = false;
  protected SuspensionState suspensionState;
  protected String authorizationUserId;
  protected String procDefId;
  protected String incidentType;
  protected String incidentId;
  protected String incidentMessage;
  protected String incidentMessageLike;

  protected String eventSubscriptionName;
  protected String eventSubscriptionType;

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessDefinitionQueryImpl processDefinitionId(String processDefinitionId) {
    this.id = processDefinitionId;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionCategory(String category) {
    if (category == null) {
      throw new ProcessEngineException("category is null");
    }
    this.category = category;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ProcessEngineException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionName(String name) {
    if (name == null) {
      throw new ProcessEngineException("name is null");
    }
    this.name = name;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ProcessEngineException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ProcessEngineException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKey(String key) {
    if (key == null) {
      throw new ProcessEngineException("key is null");
    }
    this.key = key;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ProcessEngineException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionResourceName(String resourceName) {
    if (resourceName == null) {
      throw new ProcessEngineException("resourceName is null");
    }
    this.resourceName = resourceName;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionResourceNameLike(String resourceNameLike) {
    if (resourceNameLike == null) {
      throw new ProcessEngineException("resourceNameLike is null");
    }
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionVersion(Integer version) {
    if (version == null) {
      throw new ProcessEngineException("version is null");
    } else if (version <= 0) {
      throw new ProcessEngineException("version must be positive");
    }
    this.version = version;
    return this;
  }

  public ProcessDefinitionQueryImpl latestVersion() {
    this.latest = true;
    return this;
  }

  public ProcessDefinitionQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public ProcessDefinitionQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public ProcessDefinitionQuery messageEventSubscription(String messageName) {
    return eventSubscription("message", messageName);
  }

  public ProcessDefinitionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription("message", messageName);
  }

  public ProcessDefinitionQuery processDefinitionStarter(String procDefId) {
    this.procDefId = procDefId;
    return this;
  }

  public ProcessDefinitionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ProcessEngineException("event name is null");
    }
    if(eventType == null) {
      throw new ProcessEngineException("event type is null");
    }
    this.eventSubscriptionType = eventType;
    this.eventSubscriptionName = eventName;
    return this;
  }

  public ProcessDefinitionQuery incidentType(String incidentType) {
    assertParamNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public ProcessDefinitionQuery incidentId(String incidentId) {
    assertParamNotNull("incident id", incidentId);
    this.incidentId = incidentId;
    return this;
  }

  public ProcessDefinitionQuery incidentMessage(String incidentMessage) {
    assertParamNotNull("incident message", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public ProcessDefinitionQuery incidentMessageLike(String incidentMessageLike) {
    assertParamNotNull("incident messageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  //sorting ////////////////////////////////////////////

  public ProcessDefinitionQuery orderByDeploymentId() {
    return orderBy(ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }

  public ProcessDefinitionQuery orderByProcessDefinitionKey() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
  }

  public ProcessDefinitionQuery orderByProcessDefinitionCategory() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
  }

  public ProcessDefinitionQuery orderByProcessDefinitionId() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
  }

  public ProcessDefinitionQuery orderByProcessDefinitionVersion() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
  }

  public ProcessDefinitionQuery orderByProcessDefinitionName() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
  }

  //results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionsByQueryCriteria(this, page);
  }

  public void checkQueryOk() {
    super.checkQueryOk();

    // latest() makes only sense when used with key() or keyLike()
    if (latest && ( (id != null) || (name != null) || (nameLike != null) || (version != null) || (deploymentId != null) ) ){
      throw new ProcessEngineException("Calling latest() can only be used in combination with key(String) and keyLike(String)");
    }
  }

  //getters ////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getKey() {
    return key;
  }
  public String getKeyLike() {
    return keyLike;
  }
  public Integer getVersion() {
    return version;
  }
  public boolean isLatest() {
    return latest;
  }
  public String getCategory() {
    return category;
  }
  public String getCategoryLike() {
    return categoryLike;
  }
  public String getResourceName() {
    return resourceName;
  }
  public String getResourceNameLike() {
    return resourceNameLike;
  }
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }

  public String getIncidentId() {
    return incidentId;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public String getIncidentMessageLike() {
    return incidentMessageLike;
  }

  public ProcessDefinitionQueryImpl startableByUser(String userId) {
    if (userId == null) {
      throw new ProcessEngineException("userId is null");
    }
    this.authorizationUserId = userId;
    return this;
  }

}
