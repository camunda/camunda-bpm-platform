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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * Job declaration for history cleanup.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobDeclaration extends JobDeclaration<HistoryCleanupContext, EverLivingJobEntity> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public HistoryCleanupJobDeclaration() {
    super(HistoryCleanupJobHandler.TYPE);
  }

  @Override
  protected ExecutionEntity resolveExecution(HistoryCleanupContext context) {
    return null;
  }

  @Override
  protected EverLivingJobEntity newJobInstance(HistoryCleanupContext context) {
    return new EverLivingJobEntity();
  }

  @Override
  protected void postInitialize(HistoryCleanupContext context, EverLivingJobEntity job) {
  }


  @Override
  public EverLivingJobEntity reconfigure(HistoryCleanupContext context, EverLivingJobEntity job) {
    HistoryCleanupJobHandlerConfiguration configuration = resolveJobHandlerConfiguration(context);
    job.setJobHandlerConfiguration(configuration);
    return job;
  }

  @Override
  protected HistoryCleanupJobHandlerConfiguration resolveJobHandlerConfiguration(HistoryCleanupContext context) {
    HistoryCleanupJobHandlerConfiguration config = new HistoryCleanupJobHandlerConfiguration();
    config.setImmediatelyDue(context.isImmediatelyDue());
    config.setMinuteFrom(context.getMinuteFrom());
    config.setMinuteTo(context.getMinuteTo());
    return config;
  }

  @Override
  public Date resolveDueDate(HistoryCleanupContext context) {
    return resolveDueDate(context.isImmediatelyDue());
  }

  private Date resolveDueDate(boolean isImmediatelyDue) {
    CommandContext commandContext = Context.getCommandContext();
    if (isImmediatelyDue) {
      return ClockUtil.getCurrentTime();
    } else {
      final BatchWindow currentOrNextBatchWindow = commandContext.getProcessEngineConfiguration().getBatchWindowManager()
        .getCurrentOrNextBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
      if (currentOrNextBatchWindow != null) {
        return currentOrNextBatchWindow.getStart();
      } else {
        return null;
      }
    }
  }

  public ParameterValueProvider getJobPriorityProvider() {
    long historyCleanupJobPriority = Context.getProcessEngineConfiguration()
        .getHistoryCleanupJobPriority();
    return new ConstantValueProvider(historyCleanupJobPriority);
  }
}
