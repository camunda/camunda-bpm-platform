/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.impl.tree;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import java.util.Map;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ActivityExecutionTupleWalker extends SingleReferenceWalker<ActivityExecutionTuple> {

  protected Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping;

  public ActivityExecutionTupleWalker(PvmExecutionImpl childExecution) {
    super(new ActivityExecutionTuple(childExecution.getActivity(), childExecution));
    activityExecutionMapping = childExecution.createActivityExecutionMapping();
  }

  @Override
  protected ActivityExecutionTuple nextElement() {
    ActivityExecutionTuple currentElement = getCurrentElement();
    ScopeImpl scope = currentElement.getScope().getFlowScope();
    if (scope == null) {
      return null;
    }
    else {
      PvmExecutionImpl scopeExecution = activityExecutionMapping.get(scope);
      return new ActivityExecutionTuple(scope, scopeExecution);
    }
  }
}
