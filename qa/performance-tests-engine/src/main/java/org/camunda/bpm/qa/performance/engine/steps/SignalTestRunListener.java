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
package org.camunda.bpm.qa.performance.engine.steps;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunner;

public class SignalTestRunListener implements ExecutionListener {

  public void notify(final DelegateExecution execution) throws Exception {
    final String runId = (String) execution.getVariable(PerfTestConstants.RUN_ID);
    CommandContext commandContext = Context.getCommandContext();
    if (runId != null && commandContext != null) {
      commandContext.getTransactionContext()
        .addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {
          public void execute(CommandContext commandContext) {
            // signal run after the transaction was committed
            PerfTestRunner.signalRun(runId);
          }
        });
    }
  }

}
