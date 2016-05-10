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
 * Keeps track of activity instances created in a branch of the activity instance tree from the process instance downwards
 *
 * @author Thorben Lindhauer
 */
public class MigratingActivityInstanceBranch {
  protected Map<ScopeImpl, MigratingActivityInstance> scopeInstances;

  public MigratingActivityInstanceBranch() {
    this(new HashMap<ScopeImpl, MigratingActivityInstance>());
  }

  protected MigratingActivityInstanceBranch(Map<ScopeImpl, MigratingActivityInstance> scopeInstances) {
    this.scopeInstances = scopeInstances;
  }

  public MigratingActivityInstanceBranch copy() {
    return new MigratingActivityInstanceBranch(new HashMap<ScopeImpl, MigratingActivityInstance>(scopeInstances));
  }

  public MigratingActivityInstance getInstance(ScopeImpl scope) {
    return scopeInstances.get(scope);
  }

  public boolean hasInstance(ScopeImpl scope) {
    return scopeInstances.containsKey(scope);
  }

  public void visited(MigratingActivityInstance activityInstance) {
    ScopeImpl targetScope = activityInstance.getTargetScope();
    if (targetScope.isScope()) {
      scopeInstances.put(targetScope, activityInstance);
    }
  }
}
