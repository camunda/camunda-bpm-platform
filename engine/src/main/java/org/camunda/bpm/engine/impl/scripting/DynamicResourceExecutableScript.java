/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.scripting;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.util.ResourceUtil;

/**
 * A script which resource path is dynamically determined during the execution.
 * Therefore it has to be executed in the context of an atomic operation.
 *
 * @author Sebastian Menski
 */
public class DynamicResourceExecutableScript extends DynamicExecutableScript {

  public DynamicResourceExecutableScript(Expression scriptResourceExpression, String language) {
    super(scriptResourceExpression, language);
  }

  public ExecutableScript getScript(VariableScope variableScope) {
    String scriptPath = (String) scriptExpression.getValue(variableScope);
    String scriptSource = ResourceUtil.loadResourceContent(scriptPath, getDeployment());
    return compileScript(scriptSource);
  }

  protected DeploymentEntity getDeployment() {
    return Context.getExecutionContext().getDeployment();
  }

}
