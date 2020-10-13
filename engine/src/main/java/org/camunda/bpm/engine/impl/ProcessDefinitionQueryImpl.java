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
package org.camunda.bpm.engine.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Documentation;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensurePositive;


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
  protected String[] ids;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected Date deployedAfter;
  protected Date deployedAt;
  protected String key;
  protected String[] keys;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected boolean latest = false;
  protected SuspensionState suspensionState;
  protected String authorizationUserId;
  protected List<String> cachedCandidateGroups;
  protected String procDefId;
  protected String incidentType;
  protected String incidentId;
  protected String incidentMessage;
  protected String incidentMessageLike;

  protected String eventSubscriptionName;
  protected String eventSubscriptionType;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeDefinitionsWithoutTenantId = false;

  protected boolean isVersionTagSet = false;
  protected String versionTag;
  protected String versionTagLike;

  protected boolean isStartableInTasklist = false;
  protected boolean isNotStartableInTasklist = false;
  protected boolean startablePermissionCheck = false;
  // for internal use
  protected List<PermissionCheck> processDefinitionCreatePermissionChecks = new ArrayList<PermissionCheck>();
  private boolean shouldJoinDeploymentTable = false;

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessDefinitionQueryImpl processDefinitionId(String processDefinitionId) {
    this.id = processDefinitionId;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionIdIn(String... ids) {
    this.ids = ids;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionCategory(String category) {
    ensureNotNull("category", category);
    this.category = category;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionCategoryLike(String categoryLike) {
    ensureNotNull("categoryLike", categoryLike);
    this.categoryLike = categoryLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionName(String name) {
    ensureNotNull("name", name);
    this.name = name;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionNameLike(String nameLike) {
    ensureNotNull("nameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    ensureNotNull("deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public ProcessDefinitionQueryImpl deployedAfter(Date deployedAfter) {
    ensureNotNull("deployedAfter", deployedAfter);
    shouldJoinDeploymentTable = true;
    this.deployedAfter = deployedAfter;
    return this;
  }

  public ProcessDefinitionQueryImpl deployedAt(Date deployedAt) {
    ensureNotNull("deployedAt", deployedAt);
    shouldJoinDeploymentTable = true;
    this.deployedAt = deployedAt;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKey(String key) {
    ensureNotNull("key", key);
    this.key = key;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKeysIn(String... keys) {
    ensureNotNull("keys", (Object[]) keys);
    this.keys = keys;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKeyLike(String keyLike) {
    ensureNotNull("keyLike", keyLike);
    this.keyLike = keyLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionResourceName(String resourceName) {
    ensureNotNull("resourceName", resourceName);
    this.resourceName = resourceName;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionResourceNameLike(String resourceNameLike) {
    ensureNotNull("resourceNameLike", resourceNameLike);
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionVersion(Integer version) {
    ensureNotNull("version", version);
    ensurePositive("version", version.longValue());
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
    return eventSubscription(EventType.MESSAGE, messageName);
  }

  public ProcessDefinitionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription(EventType.MESSAGE, messageName);
  }

  public ProcessDefinitionQuery processDefinitionStarter(String procDefId) {
    this.procDefId = procDefId;
    return this;
  }

  public ProcessDefinitionQuery eventSubscription(EventType eventType, String eventName) {
    ensureNotNull("event type", eventType);
    ensureNotNull("event name", eventName);
    this.eventSubscriptionType = eventType.name();
    this.eventSubscriptionName = eventName;
    return this;
  }

  public ProcessDefinitionQuery incidentType(String incidentType) {
    ensureNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public ProcessDefinitionQuery incidentId(String incidentId) {
    ensureNotNull("incident id", incidentId);
    this.incidentId = incidentId;
    return this;
  }

  public ProcessDefinitionQuery incidentMessage(String incidentMessage) {
    ensureNotNull("incident message", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public ProcessDefinitionQuery incidentMessageLike(String incidentMessageLike) {
    ensureNotNull("incident messageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions() || CompareUtil.elementIsNotContainedInArray(id, ids);
  }

  public ProcessDefinitionQueryImpl tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public ProcessDefinitionQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public ProcessDefinitionQuery includeProcessDefinitionsWithoutTenantId() {
    this.includeDefinitionsWithoutTenantId  = true;
    return this;
  }

  public ProcessDefinitionQuery versionTag(String versionTag) {
    ensureNotNull("versionTag", versionTag);
    this.versionTag = versionTag;
    this.isVersionTagSet = true;

    return this;
  }

  public ProcessDefinitionQuery versionTagLike(String versionTagLike) {
    ensureNotNull("versionTagLike", versionTagLike);
    this.versionTagLike = versionTagLike;

    return this;
  }

  public ProcessDefinitionQuery withoutVersionTag() {
    this.isVersionTagSet = true;
    this.versionTag = null;

    return this;
  }

  public ProcessDefinitionQuery startableInTasklist() {
    this.isStartableInTasklist = true;
    return this;
  }

  public ProcessDefinitionQuery notStartableInTasklist() {
    this.isNotStartableInTasklist = true;
    return this;
  }

  public ProcessDefinitionQuery startablePermissionCheck() {
    this.startablePermissionCheck = true;
    return this;
  }

  //sorting ////////////////////////////////////////////

  public ProcessDefinitionQuery orderByDeploymentId() {
    return orderBy(ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }

  public ProcessDefinitionQuery orderByDeploymentTime() {
    shouldJoinDeploymentTable = true;
    return orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_DEPLOYMENT, ProcessDefinitionQueryProperty.DEPLOY_TIME));
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

  public ProcessDefinitionQuery orderByTenantId() {
    return orderBy(ProcessDefinitionQueryProperty.TENANT_ID);
  }

  public ProcessDefinitionQuery orderByVersionTag() {
    return orderBy(ProcessDefinitionQueryProperty.VERSION_TAG);
  }

  //results ////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    // fetch candidate groups
    getCandidateGroups();
    return commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  @Override
  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    // fetch candidate groups
    getCandidateGroups();
    List<ProcessDefinition> list = commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionsByQueryCriteria(this, page);

    boolean shouldQueryAddBpmnModelInstancesToCache =
        commandContext.getProcessEngineConfiguration().getEnableFetchProcessDefinitionDescription();
    if(shouldQueryAddBpmnModelInstancesToCache) {
      addProcessDefinitionToCacheAndRetrieveDocumentation(list);
    }

    return list;
  }

  protected void addProcessDefinitionToCacheAndRetrieveDocumentation(List<ProcessDefinition> list) {
    for (ProcessDefinition processDefinition : list) {

      BpmnModelInstance bpmnModelInstance = Context.getProcessEngineConfiguration()
          .getDeploymentCache()
          .findBpmnModelInstanceForProcessDefinition((ProcessDefinitionEntity) processDefinition);

      ModelElementInstance processElement = bpmnModelInstance.getModelElementById(processDefinition.getKey());
      if (processElement != null) {
        Collection<Documentation> documentations = processElement.getChildElementsByType(Documentation.class);
        List<String> docStrings = new ArrayList<String>();
        for (Documentation documentation : documentations) {
          docStrings.add(documentation.getTextContent());
        }

        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processDefinition;
        processDefinitionEntity.setProperty(BpmnParse.PROPERTYNAME_DOCUMENTATION, BpmnParse.parseDocumentation(docStrings));
      }

    }
  }

  @Override
  public void checkQueryOk() {
    super.checkQueryOk();

    if (latest && ( (id != null) || (version != null) || (deploymentId != null) ) ){
      throw new ProcessEngineException("Calling latest() can only be used in combination with key(String) and keyLike(String) or name(String) and nameLike(String)");
    }
  }

  //getters ////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public Date getDeployedAfter() {
    return deployedAfter;
  }

  public Date getDeployedAt() {
    return deployedAt;
  }

  public String getId() {
    return id;
  }

  public String[] getIds() {
    return ids;
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

  public String getVersionTag() {
    return versionTag;
  }

  public boolean isStartableInTasklist() {
    return isStartableInTasklist;
  }

  public boolean isNotStartableInTasklist() {
    return isNotStartableInTasklist;
  }

  public boolean isStartablePermissionCheck() {
    return startablePermissionCheck;
  }

  public void setProcessDefinitionCreatePermissionChecks(List<PermissionCheck> processDefinitionCreatePermissionChecks) {
    this.processDefinitionCreatePermissionChecks = processDefinitionCreatePermissionChecks;
  }

  public List<PermissionCheck> getProcessDefinitionCreatePermissionChecks() {
    return processDefinitionCreatePermissionChecks;
  }
  
  public boolean isShouldJoinDeploymentTable() {
    return shouldJoinDeploymentTable;
  }

  public void addProcessDefinitionCreatePermissionCheck(CompositePermissionCheck processDefinitionCreatePermissionCheck) {
    processDefinitionCreatePermissionChecks.addAll(processDefinitionCreatePermissionCheck.getAllPermissionChecks());
  }

  public List<String> getCandidateGroups() {
    if (cachedCandidateGroups != null) {
      return cachedCandidateGroups;
    }

    if(authorizationUserId != null) {
      List<Group> groups = Context.getCommandContext()
          .getReadOnlyIdentityProvider()
          .createGroupQuery()
          .groupMember(authorizationUserId)
          .list();
      cachedCandidateGroups = groups.stream().map(Group::getId).collect(Collectors.toList());
    }

    return cachedCandidateGroups;
  }

  public ProcessDefinitionQueryImpl startableByUser(String userId) {
    ensureNotNull("userId", userId);
    this.authorizationUserId = userId;

    return this;
  }
}
