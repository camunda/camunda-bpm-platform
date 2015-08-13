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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Daniel Meyer
 *
 */
public class ScopeCollector implements TreeVisitor<ScopeImpl> {

  protected List<ScopeImpl> scopes = new ArrayList<ScopeImpl>();

  public void visit(ScopeImpl obj) {
    if(obj != null && obj.isScope()) {
      scopes.add(obj);
    }
  }

  public List<ScopeImpl> getScopes() {
    return scopes;
  }

}
