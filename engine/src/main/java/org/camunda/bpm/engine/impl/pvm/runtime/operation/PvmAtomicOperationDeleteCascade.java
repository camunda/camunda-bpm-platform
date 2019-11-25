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
    PvmExecutionImpl nextLeaf;
    do {
      nextLeaf = findNextLeaf(execution);

      // nextLeaf can already be removed, if it was the concurrent parent of the previous leaf.
      // In that case, DELETE_CASCADE_FIRE_ACTIVITY_END on the previousLeaf already removed
      // nextLeaf, so calling DELETE_CASCADE_FIRE_ACTIVITY_END again would incorrectly
      // invoke execution listeners
      if (nextLeaf.isDeleteRoot() && nextLeaf.isRemoved()) {
        return;
      }

      // propagate properties
      PvmExecutionImpl deleteRoot = getDeleteRoot(execution);
      if (deleteRoot != null) {
        nextLeaf.setSkipCustomListeners(deleteRoot.isSkipCustomListeners());
        nextLeaf.setSkipIoMappings(deleteRoot.isSkipIoMappings());
        nextLeaf.setExternallyTerminated(deleteRoot.isExternallyTerminated());
      }

      PvmExecutionImpl subProcessInstance = nextLeaf.getSubProcessInstance();
      if (subProcessInstance != null) {
        if (deleteRoot.isSkipSubprocesses()) {
          subProcessInstance.setSuperExecution(null);
        } else {
          subProcessInstance.deleteCascade(execution.getDeleteReason(), nextLeaf.isSkipCustomListeners(), nextLeaf.isSkipIoMappings(),
              nextLeaf.isExternallyTerminated(), nextLeaf.isSkipSubprocesses());
        }
      }

      nextLeaf.performOperation(DELETE_CASCADE_FIRE_ACTIVITY_END);

    } while (!nextLeaf.isDeleteRoot());

  }

  protected PvmExecutionImpl findNextLeaf(PvmExecutionImpl execution) {
    if (execution.hasChildren()) {
      return findNextLeaf(execution.getExecutions().get(0));
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
