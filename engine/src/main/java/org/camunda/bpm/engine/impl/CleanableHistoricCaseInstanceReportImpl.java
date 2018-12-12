/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class CleanableHistoricCaseInstanceReportImpl extends AbstractQuery<CleanableHistoricCaseInstanceReport, CleanableHistoricCaseInstanceReportResult> implements CleanableHistoricCaseInstanceReport {

  private static final long serialVersionUID = 1L;

  protected String[] caseDefinitionIdIn;
  protected String[] caseDefinitionKeyIn;
  protected String[] tenantIdIn;
  protected boolean isTenantIdSet = false;
  protected boolean isCompact = false;

  protected Date currentTimestamp;

  public CleanableHistoricCaseInstanceReportImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public CleanableHistoricCaseInstanceReport caseDefinitionIdIn(String... caseDefinitionIds) {
    ensureNotNull(NotValidException.class, "", "caseDefinitionIdIn", (Object[]) caseDefinitionIds);
    this.caseDefinitionIdIn = caseDefinitionIds;
    return this;
  }

  @Override
  public CleanableHistoricCaseInstanceReport caseDefinitionKeyIn(String... caseDefinitionKeys) {
    ensureNotNull(NotValidException.class, "", "caseDefinitionKeyIn", (Object[]) caseDefinitionKeys);
    this.caseDefinitionKeyIn = caseDefinitionKeys;
    return this;
  }

  @Override
  public CleanableHistoricCaseInstanceReport tenantIdIn(String... tenantIds) {
    ensureNotNull(NotValidException.class, "", "tenantIdIn", (Object[]) tenantIds);
    this.tenantIdIn = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  @Override
  public CleanableHistoricCaseInstanceReport withoutTenantId() {
    this.tenantIdIn = null;
    isTenantIdSet = true;
    return this;
  }

  @Override
  public CleanableHistoricCaseInstanceReport compact() {
    this.isCompact = true;
    return this;
  }

  @Override
  public CleanableHistoricCaseInstanceReport orderByFinished() {
    orderBy(CleanableHistoricInstanceReportProperty.FINISHED_AMOUNT);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
        .getHistoricCaseInstanceManager()
        .findCleanableHistoricCaseInstancesReportCountByCriteria(this);
  }

  @Override
  public List<CleanableHistoricCaseInstanceReportResult> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricCaseInstanceManager()
        .findCleanableHistoricCaseInstancesReportByCriteria(this, page);
  }

  public String[] getCaseDefinitionIdIn() {
    return caseDefinitionIdIn;
  }

  public void setCaseDefinitionIdIn(String[] caseDefinitionIdIn) {
    this.caseDefinitionIdIn = caseDefinitionIdIn;
  }

  public String[] getCaseDefinitionKeyIn() {
    return caseDefinitionKeyIn;
  }

  public void setCaseDefinitionKeyIn(String[] caseDefinitionKeyIn) {
    this.caseDefinitionKeyIn = caseDefinitionKeyIn;
  }

  public Date getCurrentTimestamp() {
    return currentTimestamp;
  }

  public void setCurrentTimestamp(Date currentTimestamp) {
    this.currentTimestamp = currentTimestamp;
  }

  public String[] getTenantIdIn() {
    return tenantIdIn;
  }

  public void setTenantIdIn(String[] tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public boolean isCompact() {
    return isCompact;
  }

}
