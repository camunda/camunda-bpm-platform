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
package org.camunda.bpm.engine.test.api.history.removaltime;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(HISTORY_FULL)
public abstract class AbstractRemovalTimeTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected FormService formService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;
  protected AuthorizationService authorizationService;

  protected static ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    formService = engineRule.getFormService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();
    decisionService = engineRule.getDecisionService();
    authorizationService = engineRule.getAuthorizationService();

    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @AfterClass
  public static void tearDownAfterAll() {
    if (processEngineConfiguration != null) {
      processEngineConfiguration
        .setHistoryRemovalTimeProvider(null)
        .setHistoryRemovalTimeStrategy(null)
        .initHistoryRemovalTime();

      processEngineConfiguration.setBatchOperationHistoryTimeToLive(null);
      processEngineConfiguration.setBatchOperationsForHistoryCleanup(null);

      processEngineConfiguration.initHistoryCleanup();

      DefaultDmnEngineConfiguration dmnEngineConfiguration =
          processEngineConfiguration.getDmnEngineConfiguration();

      ResetDmnConfigUtil.reset(dmnEngineConfiguration)
          .enableFeelLegacyBehavior(false)
          .init();

      processEngineConfiguration.setEnableHistoricInstancePermissions(false);
      processEngineConfiguration.setAuthorizationEnabled(false);
    }

    ClockUtil.reset();
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    return commandExecutor.execute(new GetByteArrayCommand(byteArrayId));
  }

  protected void clearAttachment(final Attachment attachment) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getAttachmentManager().delete((AttachmentEntity) attachment);
        return null;
      }
    });
  }

  protected void clearCommentByTaskId(final String taskId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getCommentManager().deleteCommentsByTaskId(taskId);
        return null;
      }
    });
  }

  protected void clearCommentByProcessInstanceId(final String processInstanceId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getCommentManager().deleteCommentsByProcessInstanceIds(Collections.singletonList(processInstanceId));
        return null;
      }
    });
  }

  protected void clearHistoricTaskInst(final String taskId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstanceById(taskId);
        commandContext.getHistoricIdentityLinkManager().deleteHistoricIdentityLinksLogByTaskId(taskId);
        return null;
      }
    });
  }

  protected void clearJobLog(final String jobId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        return null;
      }
    });
  }

  protected void clearHistoricIncident(final HistoricIncident historicIncident) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricIncidentManager().delete((HistoricIncidentEntity) historicIncident);
        return null;
      }
    });
  }

  protected void clearAuthorization() {
    authorizationService.createAuthorizationQuery().list()
        .forEach(authorization -> authorizationService.deleteAuthorization(authorization.getId()));
  }

  protected Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }

  protected void enabledAuth() {
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  protected void disableAuth() {
    processEngineConfiguration.setAuthorizationEnabled(false);
  }

}
