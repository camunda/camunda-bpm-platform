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
package org.camunda.bpm.engine.impl.tree;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Collects executions that execute an activity instance that is a leaf in the activity instance tree.
 * Typically, such executions are also leaves in the execution tree. The exception to this are compensation-throwing
 * executions: Their activities are leaves but they have child executions responsible for compensation handling.
 *
 * @author Thorben Lindhauer
 *
 */
public class LeafActivityInstanceExecutionCollector implements TreeVisitor<PvmExecutionImpl> {

  protected List<PvmExecutionImpl> leaves = new ArrayList<PvmExecutionImpl>();

  public void visit(PvmExecutionImpl obj) {
    if (obj.getNonEventScopeExecutions().isEmpty() || (obj.getActivity() != null && !LegacyBehavior.hasInvalidIntermediaryActivityId(obj))) {
      leaves.add(obj);
    }
  }

  public List<PvmExecutionImpl> getLeaves() {
    return leaves;
  }
}