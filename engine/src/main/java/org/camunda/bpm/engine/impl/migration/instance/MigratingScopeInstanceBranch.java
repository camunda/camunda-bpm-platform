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
package org.camunda.bpm.engine.impl.migration.instance;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Keeps track of scope instances (activity instances; event scope instances) created in a branch
 * of the activity/event scope tree from the process instance downwards
 *
 * @author Thorben Lindhauer
 */
public class MigratingScopeInstanceBranch {
  protected Map<ScopeImpl, MigratingScopeInstance> scopeInstances;

  public MigratingScopeInstanceBranch() {
    this(new HashMap<ScopeImpl, MigratingScopeInstance>());
  }

  protected MigratingScopeInstanceBranch(
      Map<ScopeImpl, MigratingScopeInstance> scopeInstances) {
    this.scopeInstances = scopeInstances;
  }

  public MigratingScopeInstanceBranch copy() {
    return new MigratingScopeInstanceBranch(
        new HashMap<ScopeImpl, MigratingScopeInstance>(scopeInstances));
  }

  public MigratingScopeInstance getInstance(ScopeImpl scope) {
    return scopeInstances.get(scope);
  }

  public boolean hasInstance(ScopeImpl scope) {
    return scopeInstances.containsKey(scope);
  }

  public void visited(MigratingScopeInstance scopeInstance) {
    ScopeImpl targetScope = scopeInstance.getTargetScope();
    if (targetScope.isScope()) {
      scopeInstances.put(targetScope, scopeInstance);
    }
  }
}
