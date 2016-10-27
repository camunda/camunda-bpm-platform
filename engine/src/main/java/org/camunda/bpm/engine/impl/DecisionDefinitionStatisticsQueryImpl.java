/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.DecisionDefinitionStatisticsImpl;
import org.camunda.bpm.engine.management.DecisionDefinitionStatistics;
import org.camunda.bpm.engine.management.DecisionDefinitionStatisticsQuery;
import org.camunda.bpm.engine.repository.DecisionDefinition;

import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class DecisionDefinitionStatisticsQueryImpl extends
    AbstractQuery<DecisionDefinitionStatisticsQuery, DecisionDefinitionStatistics> implements DecisionDefinitionStatisticsQuery {

  private static final long AT_LEAST_ONE = 1L;
  protected final String decisionRequirementsDefinitionId;
  protected String decisionInstanceId;
  protected boolean isDecisionInstanceIdSet;

  public DecisionDefinitionStatisticsQueryImpl(String decisionRequirementsDefinitionId, CommandExecutor commandExecutor) {
    super(commandExecutor);
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();

    long count = commandContext
        .getStatisticsManager()
        .getStatisticsCountGroupedByDecisionRequirementsDefinition(this);

    DecisionRequirementsDefinitionEntity definition = commandContext
        .getDecisionDefinitionManager()
        .findDecisionRequirementsDefinitionById(this.decisionRequirementsDefinitionId);
    if (count == 0 && definition != null && !isDecisionInstanceIdSet) {
      count = AT_LEAST_ONE;
    }

    return count;
  }

  @Override
  public List<DecisionDefinitionStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<DecisionDefinitionStatistics> statisticsList = commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByDecisionRequirementsDefinition(this, page);

    DecisionRequirementsDefinitionEntity definitionEntity = commandContext
        .getDecisionDefinitionManager()
        .findDecisionRequirementsDefinitionById(this.decisionRequirementsDefinitionId);

    if (statisticsList.size() == 0 && definitionEntity != null && !isDecisionInstanceIdSet) {
      List<DecisionDefinition> decisions = commandContext.getDecisionDefinitionManager().findDecisionDefinitionByDeploymentId(definitionEntity.getDeploymentId());
      for (DecisionDefinition decision : decisions) {
        DecisionDefinitionStatisticsImpl constructedEmptyStatistics = new DecisionDefinitionStatisticsImpl();
        constructedEmptyStatistics.setDecisionDefinitionId(decision.getId());
        statisticsList.add(constructedEmptyStatistics);
      }
    }
    return statisticsList;
  }

  public String getDecisionRequirementsDefinitionId() {
    return decisionRequirementsDefinitionId;
  }

  @Override
  public DecisionDefinitionStatisticsQuery decisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
    this.isDecisionInstanceIdSet = true;
    return this;
  }

  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

}
