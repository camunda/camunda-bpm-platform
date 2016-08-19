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

package org.camunda.bpm.engine.impl.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public abstract class FormDataImpl implements FormData, Serializable {

  private static final long serialVersionUID = 1L;

  protected String formKey;
  protected String deploymentId;
  protected List<FormProperty> formProperties = new ArrayList<FormProperty>();

  protected List<FormField> formFields = new ArrayList<FormField>();

  // getters and setters //////////////////////////////////////////////////////

  public String getFormKey() {
    return formKey;
  }
  public String getDeploymentId() {
    return deploymentId;
  }
  public List<FormProperty> getFormProperties() {
    return formProperties;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public void setFormProperties(List<FormProperty> formProperties) {
    this.formProperties = formProperties;
  }

  public List<FormField> getFormFields() {
    return formFields;
  }
  public void setFormFields(List<FormField> formFields) {
    this.formFields = formFields;
  }
}
