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


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.type.BooleanFormType;
import org.camunda.bpm.engine.impl.form.type.DateFormType;
import org.camunda.bpm.engine.impl.form.type.EnumFormType;
import org.camunda.bpm.engine.impl.form.type.StringFormType;

/**
 * <p>A simple {@link FormEngine} implementaiton which renders
 * forms as HTML such that they can be used as embedded forms
 * inside camunda Tasklist.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HtmlFormEngine implements FormEngine {
  protected StartFormRenderDelegate startFormRenderDelegate = new StartFormRenderDelegate();
  protected TaskFormRenderDelegate taskFormRenderDelegate = new TaskFormRenderDelegate();

  public String getName() {
    return "html";
  }

  public Object renderStartForm(StartFormData startForm) {
    return startFormRenderDelegate.renderFormData(startForm);
  }

  public Object renderTaskForm(TaskFormData taskForm) {
    return taskFormRenderDelegate.renderFormData(taskForm);
  }



}
