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
package org.camunda.bpm.engine.delegate;

/**
 * <p>A variable listener can be defined on a scope in a case model.
 * Depending on its configuration, it is invoked when a variable is create/updated/deleted
 * on a case execution that corresponds to that scope or to any of its descendant scopes.</p>
 *
 * <p>
 * <strong>Beware:</strong> If you set a variable inside a {@link VariableListener} implementation,
 * this will result in new variable listener invocations. Make sure that your implementation
 * allows to exit such a cascade as otherwise there will be an <strong>infinite loop</strong>.
 * </p>
 *
 * @author Thorben Lindhauer
 */
public interface CaseVariableListener extends VariableListener<DelegateCaseVariableInstance> {

  String CREATE = VariableListener.CREATE;
  String UPDATE = VariableListener.UPDATE;
  String DELETE = VariableListener.DELETE;

  void notify(DelegateCaseVariableInstance variableInstance) throws Exception;
}
