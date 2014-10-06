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

package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN callActivity element
 *
 * @author Sebastian Menski
 */
public class CallActivityImpl extends ActivityImpl implements CallActivity {

  protected static Attribute<String> calledElementAttribute;

  /** camunda extensions */

  protected static Attribute<Boolean> camundaAsyncAttribute;
  protected static Attribute<String> camundaCalledElementBindingAttribute;
  protected static Attribute<String> camundaCalledElementVersionAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CallActivity.class, BPMN_ELEMENT_CALL_ACTIVITY)
      .namespaceUri(BPMN20_NS)
      .extendsType(Activity.class)
      .instanceProvider(new ModelTypeInstanceProvider<CallActivity>() {
        public CallActivity newInstance(ModelTypeInstanceContext instanceContext) {
          return new CallActivityImpl(instanceContext);
        }
      });

    calledElementAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_CALLED_ELEMENT)
      .build();

    /** camunda extensions */

    camundaAsyncAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_ASYNC)
      .namespace(CAMUNDA_NS)
      .defaultValue(false)
      .build();

    camundaCalledElementBindingAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_BINDING)
      .namespace(CAMUNDA_NS)
      .build();

    camundaCalledElementVersionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_VERSION)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public CallActivityImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public CallActivityBuilder builder() {
    return new CallActivityBuilder((BpmnModelInstance) modelInstance, this);
  }

  public String getCalledElement() {
    return calledElementAttribute.getValue(this);
  }

  public void setCalledElement(String calledElement) {
    calledElementAttribute.setValue(this, calledElement);
  }

  /**
   * @deprecated use isCamundaAsyncBefore() instead.
   */
  public boolean isCamundaAsync() {
    return camundaAsyncAttribute.getValue(this);
  }

  /**
   * @deprecated use setCamundaAsyncBefore() instead.
   */
  public void setCamundaAsync(boolean isCamundaAsync) {
    camundaAsyncAttribute.setValue(this, isCamundaAsync);
  }

  public String getCamundaCalledElementBinding() {
    return camundaCalledElementBindingAttribute.getValue(this);
  }

  public void setCamundaCalledElementBinding(String camundaCalledElementBinding) {
    camundaCalledElementBindingAttribute.setValue(this, camundaCalledElementBinding);
  }

  public String getCamundaCalledElementVersion() {
    return camundaCalledElementVersionAttribute.getValue(this);
  }

  public void setCamundaCalledElementVersion(String camundaCalledElementVersion) {
    camundaCalledElementVersionAttribute.setValue(this, camundaCalledElementVersion);
  }
}
