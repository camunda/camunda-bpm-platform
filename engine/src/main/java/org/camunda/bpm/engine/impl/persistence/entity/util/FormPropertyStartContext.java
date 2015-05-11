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
package org.camunda.bpm.engine.impl.persistence.entity.util;

import org.camunda.bpm.engine.impl.form.FormPropertyHelper;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Daniel Meyer
 *
 */
public class FormPropertyStartContext extends ProcessInstanceStartContext {

  protected VariableMap formProperties;

  public FormPropertyStartContext(ActivityImpl selectedInitial) {
    super(selectedInitial);
  }

  /**
   * @param properties
   */
  public void setFormProperties(VariableMap properties) {
    this.formProperties = properties;
  }

  public void executionStarted(PvmExecutionImpl execution) {
    FormPropertyHelper.initFormPropertiesOnScope(formProperties, execution);

    // make sure create events are fired after form is submitted
    super.executionStarted(execution);
  }

}
