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

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.form.TaskFormDataImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.concurrent.Callable;


/**
 * @author Tom Baeyens
 */
public class DefaultTaskFormHandler extends DefaultFormHandler implements TaskFormHandler {

  public TaskFormData createTaskForm(final TaskEntity task) {
    ProcessDefinition processDefinition = (ProcessDefinition) task.getExecution().getProcessDefinition();
    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication(processDefinition.getDeploymentId());

    if(targetProcessApplication != null) {

      return Context.executeWithinProcessApplication(new Callable<TaskFormData>() {

        public TaskFormData call() throws Exception {
          return createTaskFormDataInternal(task);
        }

      }, targetProcessApplication);
    } else {
      return createTaskFormDataInternal(task);
    }
  }

  protected TaskFormData createTaskFormDataInternal(TaskEntity task) {
    TaskFormDataImpl taskFormData = new TaskFormDataImpl();

    Expression formKey = task.getTaskDefinition().getFormKey();

    if (formKey != null) {
      Object formValue = formKey.getValue(task);
      if (formValue != null) {
        taskFormData.setFormKey(formValue.toString());
      }
    }

    taskFormData.setDeploymentId(deploymentId);
    taskFormData.setTask(task);
    initializeFormProperties(taskFormData, task.getExecution());
    initializeFormFields(taskFormData, task.getExecution());
    return taskFormData;
  }

}
