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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Ingo Richtsmeier
 */
public class DeploymentQueryImpl extends AbstractQuery<DeploymentQuery, Deployment> implements DeploymentQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String name;
  protected String nameLike;
  protected boolean sourceQueryParamEnabled;
  protected String source;
  protected Date deploymentBefore;
  protected Date deploymentAfter;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeDeploymentsWithoutTenantId = false;

  public DeploymentQueryImpl() {
  }

  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DeploymentQueryImpl deploymentId(String deploymentId) {
    ensureNotNull("Deployment id", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public DeploymentQueryImpl deploymentName(String deploymentName) {
    ensureNotNull("deploymentName", deploymentName);
    this.name = deploymentName;
    return this;
  }

  public DeploymentQueryImpl deploymentNameLike(String nameLike) {
    ensureNotNull("deploymentNameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  public DeploymentQuery deploymentSource(String source) {
    sourceQueryParamEnabled = true;
    this.source = source;
    return this;
  }

  public DeploymentQuery deploymentBefore(Date before) {
    ensureNotNull("deploymentBefore", before);
    this.deploymentBefore = before;
    return this;
  }

  public DeploymentQuery deploymentAfter(Date after) {
    ensureNotNull("deploymentAfter", after);
    this.deploymentAfter = after;
    return this;
  }

  public DeploymentQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public DeploymentQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public DeploymentQuery includeDeploymentsWithoutTenantId() {
    this.includeDeploymentsWithoutTenantId  = true;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions() || CompareUtil.areNotInAscendingOrder(deploymentAfter, deploymentBefore);
  }

  //sorting ////////////////////////////////////////////////////////

  public DeploymentQuery orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }

  public DeploymentQuery orderByDeploymenTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }

  public DeploymentQuery orderByDeploymentTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }

  public DeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_NAME);
  }

  public DeploymentQuery orderByTenantId() {
    return orderBy(DeploymentQueryProperty.TENANT_ID);
  }

  //results ////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentCountByQueryCriteria(this);
  }

  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentsByQueryCriteria(this, page);
  }

  //getters ////////////////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public boolean isSourceQueryParamEnabled() {
    return sourceQueryParamEnabled;
  }

  public String getSource() {
    return source;
  }

  public Date getDeploymentBefore() {
    return deploymentBefore;
  }

  public Date getDeploymentAfter() {
    return deploymentAfter;
  }
}
