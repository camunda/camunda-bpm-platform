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
package org.camunda.bpm.engine.impl.form.handler;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * @author Roman Smirnov
 *
 */
public class DelegateTaskFormHandler extends DelegateFormHandler implements TaskFormHandler {

  public DelegateTaskFormHandler(TaskFormHandler formHandler, DeploymentEntity deployment) {
    super(formHandler, deployment.getId());
  }

  public TaskFormData createTaskForm(final TaskEntity task) {
    return performContextSwitch(new Callable<TaskFormData> () {
      public TaskFormData call() throws Exception {
        CreateTaskFormInvocation invocation = new CreateTaskFormInvocation((TaskFormHandler) formHandler, task);
        Context.getProcessEngineConfiguration()
            .getDelegateInterceptor()
            .handleInvocation(invocation);
        return (TaskFormData) invocation.getInvocationResult();
      }
    });
  }

  public FormHandler getFormHandler() {
    return (TaskFormHandler) formHandler;
  }

}
