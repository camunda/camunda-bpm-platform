/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.impl.test;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Base class for CMMN test cases with helper methods.
 *
 * These also includes state transition methods which are currently
 * not implemented as parted of the public API, i.e. {@link CaseService}.
 * These methods should be removed after they are available through public API.
 *
 * @author Sebastian Menski
 */
public class CmmnProcessEngineTestCase extends PluggableProcessEngineTestCase {

  // create case instance
  protected CaseInstance createCaseInstance() {
    return createCaseInstance(null);
  }

  protected CaseInstance createCaseInstance(String businessKey) {
    String caseDefinitionKey = repositoryService.
      createCaseDefinitionQuery()
      .singleResult()
      .getKey();

    return createCaseInstanceByKey(caseDefinitionKey, businessKey);
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey) {
    return createCaseInstanceByKey(caseDefinitionKey, null, null);
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey) {
    return createCaseInstanceByKey(caseDefinitionKey, businessKey, null);
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey, VariableMap variables) {
    return createCaseInstanceByKey(caseDefinitionKey, null, variables);
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey, VariableMap variables) {
    return caseService
      .withCaseDefinitionByKey(caseDefinitionKey)
      .businessKey(businessKey)
      .setVariables(variables)
      .create();
  }

  // queries

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

  // transition methods

  protected void close(final String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .close();
  }

  protected void complete(final String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .complete();
  }

  protected CaseInstance create(final String caseDefinitionId) {
    return caseService
      .withCaseDefinition(caseDefinitionId)
      .create();
  }

  protected CaseInstance create(final String caseDefinitionId, final String businessKey) {
    return caseService
      .withCaseDefinition(caseDefinitionId)
      .businessKey(businessKey)
      .create();
  }

  protected void disable(final String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();
  }

  protected void exit(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).exit();
      }
    });
  }

  protected void manualStart(final String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();
  }

  protected void occur(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).occur();
      }
    });
  }

  protected void parentResume(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).parentResume();
      }
    });
  }

  protected void parentSuspend(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).parentSuspend();

      }
    });
  }

  protected void parentTerminate(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).parentTerminate();
      }
    });
  }

  protected void reactivate(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).reactivate();
      }
    });
  }

  protected void reenable(final String caseExecutionId) {
    caseService
      .withCaseExecution(caseExecutionId)
      .reenable();
  }

  protected void resume(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).resume();
      }
    });
  }

  protected void suspend(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).suspend();
      }
    });
  }

  protected void terminate(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).terminate();
      }
    });
  }

  protected void executeHelperCaseCommand(HelperCaseCommand command) {
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(command);
  }

  protected abstract class HelperCaseCommand implements Command<Void> {

    protected CmmnExecution getExecution(String caseExecutionId) {
      return (CmmnExecution) caseService
        .createCaseExecutionQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();
    }

    public Void execute(CommandContext commandContext) {
      execute();
      return null;
    }

    public abstract void execute();

  }

}
