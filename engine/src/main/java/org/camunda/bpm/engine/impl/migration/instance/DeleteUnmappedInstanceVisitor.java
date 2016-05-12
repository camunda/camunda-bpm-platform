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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.impl.tree.TreeVisitor;

/**
 * @author Thorben Lindhauer
 *
 */
public class DeleteUnmappedInstanceVisitor implements TreeVisitor<MigratingScopeInstance> {

  protected Set<MigratingScopeInstance> visitedInstances = new HashSet<MigratingScopeInstance>();

  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public DeleteUnmappedInstanceVisitor(boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  @Override
  public void visit(MigratingScopeInstance currentInstance) {

    visitedInstances.add(currentInstance);
    if (!currentInstance.migrates()) {
      Set<MigratingProcessElementInstance> children = new HashSet<MigratingProcessElementInstance>(currentInstance.getChildren());
      MigratingScopeInstance parent = currentInstance.getParent();

      // 1. detach children
      currentInstance.detachChildren();

      // 2. manipulate execution tree (i.e. remove this instance)
      currentInstance.remove(skipCustomListeners, skipIoMappings);

      // 3. reconnect parent and children
      for (MigratingProcessElementInstance child : children) {
        child.attachState(parent);
      }
    }
    else {
      currentInstance.removeUnmappedDependentInstances();
    }
  }

  public boolean hasVisitedAll(Collection<MigratingScopeInstance> activityInstances) {
    return visitedInstances.containsAll(activityInstances);
  }
}
