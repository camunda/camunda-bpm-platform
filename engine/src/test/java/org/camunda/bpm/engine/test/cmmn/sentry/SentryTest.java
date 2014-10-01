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
package org.camunda.bpm.engine.test.cmmn.sentry;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 * @author Roman Smirnov
 *
 */
public abstract class SentryTest extends PluggableProcessEngineTestCase {

  // helper methods /////////////////////////////////////////////////////////////////////

  protected CaseInstance createCaseInstance() {
    return caseService
        .withCaseDefinitionByKey("case")
        .create();
  }

  protected void manualStart(String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();
  }

  protected void complete(String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .complete();
  }

  protected void disable(String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();
  }

  protected void suspend(final String caseExecutionId) {
    processEngineConfiguration.
    getCommandExecutorTxRequired().
    execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        CmmnExecution caseExecution = (CmmnExecution) caseService
            .createCaseExecutionQuery()
            .caseExecutionId(caseExecutionId)
            .singleResult();
        caseExecution.suspend();
        return null;
      }

    });

  }

  protected void resume(final String caseExecutionId) {
    processEngineConfiguration.
    getCommandExecutorTxRequired().
    execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        CmmnExecution caseExecution = (CmmnExecution) caseService
            .createCaseExecutionQuery()
            .caseExecutionId(caseExecutionId)
            .singleResult();
        caseExecution.resume();
        return null;
      }

    });

  }

  protected CaseExecution queryCaseExecutionByActivityId(String activityId) {
    return caseService
        .createCaseExecutionQuery()
        .activityId(activityId)
        .singleResult();
  }

  protected CaseExecution queryCaseExecutionById(String caseExecutionId) {
    return caseService
        .createCaseExecutionQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();
  }

  protected CaseSentryPartQueryImpl createCaseSentryPartQuery() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor);
  }

}
