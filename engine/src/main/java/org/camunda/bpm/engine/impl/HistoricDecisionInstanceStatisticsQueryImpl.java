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

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatistics;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;

import java.util.List;


/**
 * @author Askar Akhmerov
 */
public class HistoricDecisionInstanceStatisticsQueryImpl extends
    AbstractQuery<HistoricDecisionInstanceStatisticsQuery, HistoricDecisionInstanceStatistics> implements HistoricDecisionInstanceStatisticsQuery {

  protected final String decisionRequirementsDefinitionId;
  protected String decisionInstanceId;

  public HistoricDecisionInstanceStatisticsQueryImpl(String decisionRequirementsDefinitionId, CommandExecutor commandExecutor) {
    super(commandExecutor);
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();

    long count = commandContext
        .getStatisticsManager()
        .getStatisticsCountGroupedByDecisionRequirementsDefinition(this);

    return count;
  }

  @Override
  public List<HistoricDecisionInstanceStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();

    List<HistoricDecisionInstanceStatistics> statisticsList = commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByDecisionRequirementsDefinition(this, page);

    return statisticsList;
  }

  protected void checkQueryOk() {
    super.checkQueryOk();
    EnsureUtil.ensureNotNull("decisionRequirementsDefinitionId", decisionRequirementsDefinitionId);
  }

  public String getDecisionRequirementsDefinitionId() {
    return decisionRequirementsDefinitionId;
  }

  @Override
  public HistoricDecisionInstanceStatisticsQuery decisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
    return this;
  }

  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

}
