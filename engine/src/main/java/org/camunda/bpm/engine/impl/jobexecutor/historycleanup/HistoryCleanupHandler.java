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

import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Tassilo Weidner
 */
public abstract class HistoryCleanupHandler implements TransactionListener {

  /**
   * Maximum allowed batch size.
   */
  public final static int MAX_BATCH_SIZE = 500;

  protected HistoryCleanupJobHandlerConfiguration configuration;
  protected String jobId;
  protected CommandExecutor commandExecutor;

  public void execute(CommandContext commandContext) {
    // passed commandContext may be in an inconsistent state
    commandExecutor.execute(new HistoryCleanupHandlerCmd());
  }

  abstract void performCleanup();

  abstract Map<String, Long> reportMetrics();

  abstract boolean shouldRescheduleNow();

  public HistoryCleanupJobHandlerConfiguration getConfiguration() {
    return configuration;
  }

  public HistoryCleanupHandler setConfiguration(HistoryCleanupJobHandlerConfiguration configuration) {
    this.configuration = configuration;
    return this;
  }

  public HistoryCleanupHandler setJobId(String jobId) {
    this.jobId = jobId;
    return this;
  }

  public HistoryCleanupHandler setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  protected class HistoryCleanupHandlerCmd implements Command<Void> {
    @Override
    public Void execute(CommandContext commandContext) {

      Map<String, Long> report = reportMetrics();
      boolean isRescheduleNow = shouldRescheduleNow();

      new HistoryCleanupSchedulerCmd(isRescheduleNow, report, configuration, jobId).execute(commandContext);

      return null;
    }
  }

}
