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
package org.camunda.bpm.engine.impl.bpmn.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocation;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;

/**
 * An {@link ExecutionListener} which invokes a {@link ExecutableScript} when notified.
 *
 * @author Sebastian Menski
 */
public class ScriptExecutionListener implements ExecutionListener {

  protected final ExecutableScript script;

  public ScriptExecutionListener(ExecutableScript script) {
    this.script = script;
  }

  public void notify(DelegateExecution execution) throws Exception {
    ScriptInvocation invocation = new ScriptInvocation(script, execution);
    Context
      .getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(invocation);
  }

  public ExecutableScript getScript() {
    return script;
  }
}
