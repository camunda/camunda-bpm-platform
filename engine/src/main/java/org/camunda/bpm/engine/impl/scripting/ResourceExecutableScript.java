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

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.util.ResourceUtil;

/**
 * A script which is provided by an external resource.
 *
 * @author Sebastian Menski
 */
public class ResourceExecutableScript extends SourceExecutableScript {

  protected String scriptResource;

  public ResourceExecutableScript(String language, String scriptResource) {
    super(language, null);
    this.scriptResource = scriptResource;
  }

  @Override
  public Object evaluate(ScriptEngine engine, VariableScope variableScope, Bindings bindings) {
    if (scriptSource == null) {
      loadScriptSource();
    }
    return super.evaluate(engine, variableScope, bindings);
  }

  protected synchronized void loadScriptSource() {
    if (getScriptSource() == null) {
      DeploymentEntity deployment = Context.getCoreExecutionContext().getDeployment();
      String source = ResourceUtil.loadResourceContent(scriptResource, deployment);
      setScriptSource(source);
    }
  }

}
