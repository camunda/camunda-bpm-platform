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


import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;

/**
 * <p>A simple {@link FormEngine} implementaiton which renders
 * forms as HTML such that they can be used as embedded forms
 * inside camunda Tasklist.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HtmlFormEngine implements FormEngine {

  public String getName() {
    return "html";
  }

  public Object renderStartForm(StartFormData startForm) {
    return renderFormData(startForm);
  }

  public Object renderTaskForm(TaskFormData taskForm) {
    return renderFormData(taskForm);
  }

  protected String renderFormData(FormData formData) {

    if(formData == null || formData.getFormFields() == null || formData.getFormFields().isEmpty()) {
      return null;

    } else {
      HtmlDocumentBuilder documentBuilder = new HtmlDocumentBuilder(new HtmlElementWriter("form").attribute("class", "form-horizontal"));

      // render fields
      for (FormField formField : formData.getFormFields()) {
        renderFormField(formField, documentBuilder);
      }

      // end document element
      documentBuilder.endElement();

      return documentBuilder.getHtmlString();

    }
  }

  protected void renderFormField(FormField formField, HtmlDocumentBuilder documentBuilder) {
    // start group
    documentBuilder.startElement(new HtmlElementWriter("div").attribute("class", "control-group"));

    // write label
    documentBuilder.startElement(new HtmlElementWriter("label").attribute("class", "control-label").textContent(formField.getName()))
      .endElement();

    // start controls
    documentBuilder.startElement(new HtmlElementWriter("div").attribute("class", "controls"));

    // write input
    HtmlElementWriter inputElement = new HtmlElementWriter("input", true)
      .attribute("form-field", null)
      .attribute("type", formField.getTypeName())
      .attribute("name", formField.getId());

    // add default value
    Object defaultValue = formField.getDefaultValue();
    if(defaultValue != null) {
      inputElement.attribute("value", defaultValue.toString());
    }

    // add validation constraints
    for (FormFieldValidationConstraint constraint : formField.getValidationConstraints()) {
      inputElement.attribute(constraint.getName(), (String) constraint.getConfiguration());
    }

    documentBuilder.startElement(inputElement).endElement();

    // end controls
    documentBuilder.endElement();

    // end group
    documentBuilder.endElement();
  }

}
