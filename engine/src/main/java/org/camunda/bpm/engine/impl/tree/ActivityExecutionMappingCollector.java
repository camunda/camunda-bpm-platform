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

package org.camunda.bpm.engine.impl.tree;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Collect the mappings of scopes and executions. It can be used to collect the mappings over process instances.
 *
 * @see ActivityExecution#createActivityExecutionMapping()
 *
 * @author Philipp Ossler
 *
 */
public class ActivityExecutionMappingCollector implements TreeVisitor<ActivityExecution> {

  private final Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = new HashMap<ScopeImpl, PvmExecutionImpl>();

  private final ActivityExecution initialExecution;
  private boolean initialized = false;

  public ActivityExecutionMappingCollector(ActivityExecution execution) {
    this.initialExecution = execution;
  }

  @Override
  public void visit(ActivityExecution execution) {
    if (!initialized) {
      // lazy initialization to avoid exceptions on creation
      appendActivityExecutionMapping(initialExecution);
      initialized = true;
    }

    appendActivityExecutionMapping(execution);
  }

  private void appendActivityExecutionMapping(ActivityExecution execution) {
    if (execution.getActivity() != null && !LegacyBehavior.hasInvalidIntermediaryActivityId((PvmExecutionImpl) execution)) {
      activityExecutionMapping.putAll(execution.createActivityExecutionMapping());
    }
  }

  /**
   * @return the mapped execution for scope or <code>null</code>, if no mapping exists
   */
  public PvmExecutionImpl getExecutionForScope(PvmScope scope) {
    return activityExecutionMapping.get(scope);
  }
}