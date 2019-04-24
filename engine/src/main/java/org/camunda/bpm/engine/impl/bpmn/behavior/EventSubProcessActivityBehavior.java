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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;

/**
 * @author Daniel Meyer
 *
 */
public class EventSubProcessActivityBehavior extends SubProcessActivityBehavior {

  public void complete(ActivityExecution scopeExecution) {
    // check whether legacy behavior needs to be performed.
    if(!LegacyBehavior.eventSubprocessComplete(scopeExecution)) {
      // in case legacy behavior is not performed, the event subprocess behaves in the same way as a regular subprocess.
      super.complete(scopeExecution);
    }
  }

  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    // Check whether legacy behavior needs to be performed.
    // Legacy behavior means that the event subprocess is not a scope and as a result does not
    // join concurrent executions on it's own. Instead it delegates to the the subprocess activity behavior in which it is embedded.
    if(!LegacyBehavior.eventSubprocessConcurrentChildExecutionEnded(scopeExecution, endedExecution)) {
      // in case legacy behavior is not performed, the event subprocess behaves in the same way as a regular subprocess.
      super.concurrentChildExecutionEnded(scopeExecution, endedExecution);
    }
  }

}
