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
package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;

/**
 * @author Daniel Meyer
 *
 */
public class ContextLogger extends ProcessEngineLogger {

  public void debugExecutingAtomicOperation(CoreAtomicOperation<?> executionOperation, CoreExecution execution) {
    logDebug(
        "001",
        "Executing atomic operation {} on {}", executionOperation, execution);
  }

  public void debugException(Throwable throwable) {
    logDebug(
        "002",
        "Exception while closing command context: {}",throwable.getMessage(), throwable);
  }

  public void infoException(Throwable throwable) {
    logInfo(
        "003",
        "Exception while closing command context: {}",throwable.getMessage(), throwable);
  }

  public void errorException(Throwable throwable) {
    logError(
        "004",
        "Exception while closing command context: {}",throwable.getMessage(), throwable);
  }

  public void exceptionWhileInvokingOnCommandFailed(Throwable t) {
    logError(
        "005",
        "Exception while invoking onCommandFailed()", t);
  }

  public void bpmnStackTrace(String string) {
    log(Context.getProcessEngineConfiguration().getLogLevelBpmnStackTrace(),
        "006",
        string);
  }

}
