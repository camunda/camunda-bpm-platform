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

package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationDeleteCascade implements PvmAtomicOperation {

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  public boolean isAsyncCapable() {
    return false;
  }

  public void execute(PvmExecutionImpl execution) {
    PvmExecutionImpl firstLeaf = findFirstLeaf(execution);

    // propagate skipCustomListeners property
    PvmExecutionImpl deleteRoot = getDeleteRoot(execution);
    if(deleteRoot != null) {
      firstLeaf.setSkipCustomListeners(deleteRoot.isSkipCustomListeners());
      firstLeaf.setSkipIoMappings(deleteRoot.isSkipIoMappings());
    }

    if (firstLeaf.getSubProcessInstance()!=null) {
      firstLeaf.getSubProcessInstance().deleteCascade(execution.getDeleteReason(), firstLeaf.isSkipCustomListeners());
    }

    firstLeaf.performOperation(DELETE_CASCADE_FIRE_ACTIVITY_END);
  }

  protected PvmExecutionImpl findFirstLeaf(PvmExecutionImpl execution) {
    if (execution.hasChildren()) {
      return findFirstLeaf(execution.getExecutions().get(0));
    }
    return execution;
  }

  protected PvmExecutionImpl getDeleteRoot(PvmExecutionImpl execution) {
    if(execution == null) {
      return null;
    } else if(execution.isDeleteRoot()) {
      return execution;
    } else {
      return getDeleteRoot(execution.getParent());
    }
  }

  public String getCanonicalName() {
    return "delete-cascade";
  }

}
