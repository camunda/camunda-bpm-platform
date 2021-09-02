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

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class TransitionInstanceCancellationCmd extends AbstractInstanceCancellationCmd {

  protected String transitionInstanceId;

  public TransitionInstanceCancellationCmd(String processInstanceId, String transitionInstanceId) {
    super(processInstanceId);
    this.transitionInstanceId = transitionInstanceId;

  }

  public String getTransitionInstanceId() {
    return transitionInstanceId;
  }

  protected ExecutionEntity determineSourceInstanceExecution(final CommandContext commandContext) {
    ActivityInstance instance = commandContext.runWithoutAuthorization(new GetActivityInstanceCmd(processInstanceId));
    TransitionInstance instanceToCancel = findTransitionInstance(instance, transitionInstanceId);
    EnsureUtil.ensureNotNull(NotValidException.class,
        describeFailure("Transition instance '" + transitionInstanceId + "' does not exist"),
        "transitionInstance",
        instanceToCancel);

    ExecutionEntity transitionExecution = commandContext.getExecutionManager().findExecutionById(instanceToCancel.getExecutionId());

    return transitionExecution;
  }

  protected String describe() {
    return "Cancel transition instance '" + transitionInstanceId + "'";
  }


}
