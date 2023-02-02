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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.TransactionLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeleteDeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;

/**
 * @author Joram Barrez
 * @author Thorben Lindhauer
 */
public class DeleteDeploymentCmd implements Command<Void>, Serializable {

  private final static TransactionLogger TX_LOG = ProcessEngineLogger.TX_LOGGER;

  private static final long serialVersionUID = 1L;

  protected String deploymentId;
  protected boolean cascade;

  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public DeleteDeploymentCmd(String deploymentId, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    this.deploymentId = deploymentId;
    this.cascade = cascade;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  public Void execute(final CommandContext commandContext) {
    ensureNotNull("deploymentId", deploymentId);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteDeployment(deploymentId);
    }

    UserOperationLogManager logManager = commandContext.getOperationLogManager();
    List<PropertyChange> propertyChanges = Arrays.asList(new PropertyChange("cascade", null, cascade));
    DeploymentEntity deployment = commandContext.getDeploymentManager().findDeploymentById(deploymentId);
    String tenantId = deployment != null ? deployment.getTenantId() : null;
    logManager.logDeploymentOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, deploymentId, tenantId, propertyChanges);

    commandContext
      .getDeploymentManager()
      .deleteDeployment(deploymentId, cascade, skipCustomListeners, skipIoMappings);

    ProcessApplicationReference processApplicationReference = Context
      .getProcessEngineConfiguration()
      .getProcessApplicationManager()
      .getProcessApplicationForDeployment(deploymentId);

    DeleteDeploymentFailListener listener = new DeleteDeploymentFailListener(deploymentId, processApplicationReference,
      Context.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew());

    try {
      commandContext.runWithoutAuthorization(new UnregisterProcessApplicationCmd(deploymentId, false));
      commandContext.runWithoutAuthorization(new UnregisterDeploymentCmd(Collections.singleton(deploymentId)));
    } finally {
      try {
        commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, listener);
      }
      catch (Exception e) {
        TX_LOG.debugTransactionOperation("Could not register transaction synchronization. Probably the TX has already been rolled back by application code.");
        listener.execute(commandContext);
      }
    }


    return null;
  }
}
