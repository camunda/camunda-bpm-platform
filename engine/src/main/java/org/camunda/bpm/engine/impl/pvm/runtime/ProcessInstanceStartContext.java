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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker.WalkCondition;
import org.camunda.bpm.engine.impl.tree.ScopeCollector;

/**
 * Callback for being notified when a model instance has started.
 *
 * @author Daniel Meyer
 *
 */
public class ProcessInstanceStartContext extends ExecutionStartContext {

  protected ActivityImpl initial;

  protected InstantiationStack instantiationStack;

  /**
   * @param initial
   */
  public ProcessInstanceStartContext(ActivityImpl initial) {
    this.initial = initial;
  }

  public ActivityImpl getInitial() {
    return initial;
  }

  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public InstantiationStack getInstantiationStack() {

    if (instantiationStack == null) {
      FlowScopeWalker flowScopeWalker = new FlowScopeWalker(initial.getFlowScope());
      ScopeCollector scopeCollector = new ScopeCollector();
      flowScopeWalker.addPreVisitor(scopeCollector).walkWhile(new ReferenceWalker.WalkCondition<ScopeImpl>() {
        public boolean isFulfilled(ScopeImpl element) {
          return element == null || element == initial.getProcessDefinition();
        }
      });

      List<PvmActivity> scopeActivities = (List) scopeCollector.getScopes();
      Collections.reverse(scopeActivities);

      instantiationStack = new InstantiationStack(scopeActivities, initial, null);
    }

    return instantiationStack;
  }

  public boolean isAsync() {
    return initial.isAsyncBefore();
  }

}
