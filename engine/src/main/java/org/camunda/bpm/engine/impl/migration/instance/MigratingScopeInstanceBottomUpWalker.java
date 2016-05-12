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

import org.camunda.bpm.engine.impl.tree.SingleReferenceWalker;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingScopeInstanceBottomUpWalker extends SingleReferenceWalker<MigratingScopeInstance> {

  protected MigratingScopeInstance parent = null;

  public MigratingScopeInstanceBottomUpWalker(MigratingScopeInstance initialElement) {
    super(initialElement);
    // determine parent beforehand since it may be removed while walking
    parent = initialElement.getParent();
  }

  @Override
  protected MigratingScopeInstance nextElement() {
    MigratingScopeInstance nextElement = parent;
    if (parent != null) {
      parent = parent.getParent();
    }
    return nextElement;
  }
}