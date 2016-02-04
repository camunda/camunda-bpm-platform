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
package org.camunda.qa.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.TestHelper;

/**
 * @author Daniel Meyer
 *
 */
public class EnsureCleanDbPlugin implements BpmPlatformPlugin {

  private AtomicInteger counter = new AtomicInteger();

  @Override
  public void postProcessApplicationDeploy(ProcessApplicationInterface processApplication) {
    counter.incrementAndGet();
  }

  @Override
  public void postProcessApplicationUndeploy(ProcessApplicationInterface processApplication) {

    // some tests deploy multiple PAs. => only check for clean DB after last PA is undeployed
    if(counter.decrementAndGet() == 0) {

      final ProcessEngine defaultProcessEngine = BpmPlatform.getDefaultProcessEngine();
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) defaultProcessEngine.getProcessEngineConfiguration();
      processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(new Command<Void>() {

          public Void execute(CommandContext commandContext) {
            TransactionContext transactionContext = commandContext.getTransactionContext();
            transactionContext.addTransactionListener(TransactionState.COMMITTED, createTxListener(defaultProcessEngine));
            transactionContext.addTransactionListener(TransactionState.ROLLED_BACK, createTxListener(defaultProcessEngine));
            return null;
          }

        });

    }
  }

  private TransactionListener createTxListener(final ProcessEngine defaultProcessEngine) {
    return new TransactionListener() {

      @Override
      public void execute(CommandContext commandContext) {

        try {
          System.out.println("Ensure cleanup after integration test ");
          TestHelper.assertAndEnsureCleanDbAndCache(defaultProcessEngine, false);
        }
        catch(Throwable e) {
          System.err.println("Could not clean DB:");
          e.printStackTrace();
        }

      }
    };
  }
}
