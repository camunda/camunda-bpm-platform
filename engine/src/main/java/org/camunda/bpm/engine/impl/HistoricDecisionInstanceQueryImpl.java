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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 *  @author Philipp Ossler
 */
public class HistoricDecisionInstanceQueryImpl extends AbstractQuery<HistoricDecisionInstanceQuery, HistoricDecisionInstance> implements HistoricDecisionInstanceQuery {

  private static final long serialVersionUID = 1L;

  protected String decisionInstanceId;
  protected String[] decisionInstanceIdIn;

  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected String processInstanceId;

  protected String[] activityInstanceIds;
  protected String[] activityIds;

  protected Date evaluatedBefore;
  protected Date evaluatedAfter;

  protected boolean includeInput = false;
  protected boolean includeOutputs = false;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public HistoricDecisionInstanceQueryImpl() {
  }

  public HistoricDecisionInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDecisionInstanceQuery decisionInstanceId(String decisionInstanceId) {
    ensureNotNull(NotValidException.class, "decisionInstanceId", decisionInstanceId);
    this.decisionInstanceId = decisionInstanceId;
    return this;
  }

  public HistoricDecisionInstanceQuery decisionInstanceIdIn(String... decisionInstanceIdIn) {
    ensureNotNull("decisionInstanceIdIn", (Object[]) decisionInstanceIdIn);
    this.decisionInstanceIdIn = decisionInstanceIdIn;
    return this;
  }

  public HistoricDecisionInstanceQuery decisionDefinitionId(String decisionDefinitionId) {
    ensureNotNull(NotValidException.class, "decisionDefinitionId", decisionDefinitionId);
    this.decisionDefinitionId = decisionDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionKey(String decisionDefinitionKey) {
    ensureNotNull(NotValidException.class, "decisionDefinitionKey", decisionDefinitionKey);
    this.decisionDefinitionKey = decisionDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionName(String decisionDefinitionName) {
    ensureNotNull(NotValidException.class, "decisionDefinitionName", decisionDefinitionName);
    this.decisionDefinitionName = decisionDefinitionName;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull(NotValidException.class, "processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull(NotValidException.class, "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processInstanceId(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery activityIdIn(String... activityIds) {
    ensureNotNull("activityIds", (Object[]) activityIds);
    this.activityIds = activityIds;
    return this;
  }

  public HistoricDecisionInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull("activityInstanceIds", (Object[]) activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricDecisionInstanceQuery evaluatedBefore(Date evaluatedBefore) {
    ensureNotNull(NotValidException.class, "evaluatedBefore", evaluatedBefore);
    this.evaluatedBefore = evaluatedBefore;
    return this;
  }

  public HistoricDecisionInstanceQuery evaluatedAfter(Date evaluatedAfter) {
    ensureNotNull(NotValidException.class, "evaluatedAfter", evaluatedAfter);
    this.evaluatedAfter = evaluatedAfter;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery orderByEvaluationTime() {
    orderBy(HistoricDecisionInstanceQueryProperty.EVALUATION_TIME);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDecisionInstanceManager()
      .findHistoricDecisionInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricDecisionInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstancesByQueryCriteria(this, page);
  }

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public HistoricDecisionInstanceQuery includeInputs() {
    includeInput = true;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery includeOutputs() {
    includeOutputs = true;
    return this;
  }

  public boolean isIncludeInput() {
    return includeInput;
  }

  public boolean isIncludeOutputs() {
    return includeOutputs;
  }

  @Override
  public HistoricDecisionInstanceQuery disableBinaryFetching() {
    isByteArrayFetchingEnabled = false;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery disableCustomObjectDeserialization() {
    isCustomObjectDeserializationEnabled = false;
    return null;
  }

  public boolean isByteArrayFetchingEnabled() {
    return isByteArrayFetchingEnabled;
  }

  public boolean isCustomObjectDeserializationEnabled() {
    return isCustomObjectDeserializationEnabled;
  }
}
