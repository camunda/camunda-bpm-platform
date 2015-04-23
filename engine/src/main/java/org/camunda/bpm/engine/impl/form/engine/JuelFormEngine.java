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
package org.camunda.bpm.engine.impl.form.engine;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.UnsupportedEncodingException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocation;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;


/**
 * @author Tom Baeyens
 */
public class JuelFormEngine implements FormEngine {

  public String getName() {
    return "juel";
  }

  public Object renderStartForm(StartFormData startForm) {
    if (startForm.getFormKey()==null) {
      return null;
    }
    String formTemplateString = getFormTemplateString(startForm, startForm.getFormKey());
    return executeScript(formTemplateString, null);
  }


  public Object renderTaskForm(TaskFormData taskForm) {
    if (taskForm.getFormKey()==null) {
      return null;
    }
    String formTemplateString = getFormTemplateString(taskForm, taskForm.getFormKey());
    TaskEntity task = (TaskEntity) taskForm.getTask();
    return executeScript(formTemplateString, task.getExecution());
  }

  protected Object executeScript(String scriptSrc, VariableScope scope) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ScriptFactory scriptFactory = processEngineConfiguration.getScriptFactory();
    ExecutableScript script = scriptFactory.createScriptFromSource(ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, scriptSrc);

    ScriptInvocation invocation = new ScriptInvocation(script, scope);
    try {
      processEngineConfiguration
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    }

    return invocation.getInvocationResult();
  }

  protected String getFormTemplateString(FormData formInstance, String formKey) {
    String deploymentId = formInstance.getDeploymentId();

    ResourceEntity resourceStream = Context
      .getCommandContext()
      .getResourceManager()
      .findResourceByDeploymentIdAndResourceName(deploymentId, formKey);

    ensureNotNull("Form with formKey '" + formKey + "' does not exist", "resourceStream", resourceStream);

    byte[] resourceBytes = resourceStream.getBytes();
    String encoding = "UTF-8";
    String formTemplateString = "";
    try {
      formTemplateString = new String(resourceBytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new ProcessEngineException("Unsupported encoding of :" + encoding, e);
    }
    return formTemplateString;
  }
}
