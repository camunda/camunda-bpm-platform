/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExecutionTopDownWalker extends ReferenceWalker<PvmExecutionImpl> {

  public ExecutionTopDownWalker(PvmExecutionImpl initialElement) {
    super(initialElement);
  }

  public ExecutionTopDownWalker(List<PvmExecutionImpl> initialElements) {
    super(initialElements);
  }

  @Override
  protected Collection<PvmExecutionImpl> nextElements() {
    List<? extends  PvmExecutionImpl> executions = getCurrentElement().getExecutions();
    if (executions == null) {
      executions = new ArrayList<PvmExecutionImpl>();
    }
    return (List) executions;
  }
}
