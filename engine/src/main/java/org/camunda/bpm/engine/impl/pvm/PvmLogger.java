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
package org.camunda.bpm.engine.impl.pvm;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 *
 */
public class PvmLogger extends ProcessEngineLogger {

  public void notTakingTranistion(PvmTransition outgoingTransition) {
    logDebug(
        "001", "Not taking transition '{}', outgoing execution has ended.", outgoingTransition);
  }

  public void debugExecutesActivity(PvmExecutionImpl execution, ActivityImpl activity, String name) {
    logDebug(
        "002", "{} executed activity {}: {}", execution, activity, name);
  }

  public void debugLeavesActivityInstance(PvmExecutionImpl execution, String activityInstanceId) {
    logDebug(
        "003", "Execution {} leaves activity instance {}", execution, activityInstanceId);
  }

  public void debugDestroyScope(PvmExecutionImpl execution, PvmExecutionImpl propagatingExecution) {
    logDebug(
        "004",
        "Execution {} leaves parent scope {}", execution, propagatingExecution);
  }

  public void destroying(PvmExecutionImpl pvmExecutionImpl) {
    logDebug(
        "005", "Detroying scope {}", pvmExecutionImpl);
  }

  public void removingEventScope(PvmExecutionImpl childExecution) {
    logDebug(
        "006", "Removeing event scope {}", childExecution);
  }

  public void interruptingExecution(String reason, boolean skipCustomListeners) {
    logDebug(
        "007", "Interrupting execution execution {}, {}", reason, skipCustomListeners);
  }

  public void debugEnterActivityInstance(PvmExecutionImpl pvmExecutionImpl, String parentActivityInstanceId) {
    logDebug(
        "008", "Enter activity instance {} parent: {}", pvmExecutionImpl, parentActivityInstanceId);
  }

  public void execptionWhileCompletingSupProcess(PvmExecutionImpl execution, Exception e) {
    logError(
        "009", "Execption while completing subprocess of execution {}", execution, e);
  }

  public void createScope(PvmExecutionImpl execution, PvmExecutionImpl propagatingExecution) {
    logDebug(
        "010", "Create scope: parent exection {} continues as  {}", execution, propagatingExecution);
  }

}
