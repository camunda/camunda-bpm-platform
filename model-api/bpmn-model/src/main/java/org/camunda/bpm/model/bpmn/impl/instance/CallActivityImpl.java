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

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ASYNC;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_BINDING;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_TENANT_ID;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_VERSION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_VERSION_TAG;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CASE_BINDING;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CASE_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CASE_TENANT_ID;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CASE_VERSION;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_VARIABLE_MAPPING_CLASS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_VARIABLE_MAPPING_DELEGATE_EXPRESSION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

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
  protected static Attribute<String> camundaCalledElementVersionTagAttribute;
  protected static Attribute<String> camundaCalledElementTenantIdAttribute;

  protected static Attribute<String> camundaCaseRefAttribute;
  protected static Attribute<String> camundaCaseBindingAttribute;
  protected static Attribute<String> camundaCaseVersionAttribute;
  protected static Attribute<String> camundaCaseTenantIdAttribute;
  protected static Attribute<String> camundaVariableMappingClassAttribute;
  protected static Attribute<String> camundaVariableMappingDelegateExpressionAttribute;

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

    camundaCalledElementVersionTagAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_VERSION_TAG)
      .namespace(CAMUNDA_NS)
      .build();

    camundaCaseRefAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CASE_REF)
       .namespace(CAMUNDA_NS)
       .build();

    camundaCaseBindingAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CASE_BINDING)
        .namespace(CAMUNDA_NS)
        .build();

    camundaCaseVersionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CASE_VERSION)
        .namespace(CAMUNDA_NS)
        .build();

    camundaCalledElementTenantIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CALLED_ELEMENT_TENANT_ID)
        .namespace(CAMUNDA_NS)
        .build();

    camundaCaseTenantIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CASE_TENANT_ID)
        .namespace(CAMUNDA_NS)
        .build();

    camundaVariableMappingClassAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLE_MAPPING_CLASS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaVariableMappingDelegateExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLE_MAPPING_DELEGATE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();


    typeBuilder.build();
  }

  public CallActivityImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
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
  @Deprecated
  public boolean isCamundaAsync() {
    return camundaAsyncAttribute.getValue(this);
  }

  /**
   * @deprecated use setCamundaAsyncBefore() instead.
   */
  @Deprecated
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

  public String getCamundaCalledElementVersionTag() {
    return camundaCalledElementVersionTagAttribute.getValue(this);
  }

  public void setCamundaCalledElementVersionTag(String camundaCalledElementVersionTag) {
    camundaCalledElementVersionTagAttribute.setValue(this, camundaCalledElementVersionTag);
  }

  public String getCamundaCaseRef() {
    return camundaCaseRefAttribute.getValue(this);
  }

  public void setCamundaCaseRef(String camundaCaseRef) {
    camundaCaseRefAttribute.setValue(this, camundaCaseRef);
  }

  public String getCamundaCaseBinding() {
    return camundaCaseBindingAttribute.getValue(this);
  }

  public void setCamundaCaseBinding(String camundaCaseBinding) {
    camundaCaseBindingAttribute.setValue(this, camundaCaseBinding);
  }

  public String getCamundaCaseVersion() {
    return camundaCaseVersionAttribute.getValue(this);
  }

  public void setCamundaCaseVersion(String camundaCaseVersion) {
    camundaCaseVersionAttribute.setValue(this, camundaCaseVersion);
  }

  public String getCamundaCalledElementTenantId() {
    return camundaCalledElementTenantIdAttribute.getValue(this);
  }

  public void setCamundaCalledElementTenantId(String tenantId) {
    camundaCalledElementTenantIdAttribute.setValue(this, tenantId);
  }

  public String getCamundaCaseTenantId() {
    return camundaCaseTenantIdAttribute.getValue(this);
  }

  public void setCamundaCaseTenantId(String tenantId) {
    camundaCaseTenantIdAttribute.setValue(this, tenantId);
  }

  @Override
  public String getCamundaVariableMappingClass() {
    return camundaVariableMappingClassAttribute.getValue(this);
  }

  @Override
  public void setCamundaVariableMappingClass(String camundaClass) {
    camundaVariableMappingClassAttribute.setValue(this, camundaClass);
  }

  @Override
  public String getCamundaVariableMappingDelegateExpression() {
    return camundaVariableMappingDelegateExpressionAttribute.getValue(this);
  }

  @Override
  public void setCamundaVariableMappingDelegateExpression(String camundaExpression) {
    camundaVariableMappingDelegateExpressionAttribute.setValue(this, camundaExpression);
  }
}
