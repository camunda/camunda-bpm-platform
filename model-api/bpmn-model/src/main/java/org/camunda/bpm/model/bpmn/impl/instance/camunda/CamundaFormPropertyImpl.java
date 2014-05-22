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

package org.camunda.bpm.model.bpmn.impl.instance.camunda;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormProperty;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaValue;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN formProperty camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaFormPropertyImpl extends BpmnModelElementInstanceImpl implements CamundaFormProperty {

  protected static Attribute<String> camundaIdAttribute;
  protected static Attribute<String> camundaNameAttribute;
  protected static Attribute<String> camundaTypeAttribute;
  protected static Attribute<Boolean> camundaRequiredAttribute;
  protected static Attribute<Boolean> camundaReadableAttribute;
  protected static Attribute<Boolean> camundaWriteableAttribute;
  protected static Attribute<String> camundaVariableAttribute;
  protected static Attribute<String> camundaExpressionAttribute;
  protected static Attribute<String> camundaDatePatternAttribute;
  protected static Attribute<String> camundaDefaultAttribute;
  protected static ChildElementCollection<CamundaValue> camundaValueCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaFormProperty.class, CAMUNDA_ELEMENT_FORM_PROPERTY)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaFormProperty>() {
        public CamundaFormProperty newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaFormPropertyImpl(instanceContext);
        }
      });

    camundaIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ID)
      .namespace(CAMUNDA_NS)
      .build();

    camundaNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_NAME)
      .namespace(CAMUNDA_NS)
      .build();

    camundaTypeAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TYPE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaRequiredAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_REQUIRED)
      .namespace(CAMUNDA_NS)
      .defaultValue(false)
      .build();

    camundaReadableAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_READABLE)
      .namespace(CAMUNDA_NS)
      .defaultValue(true)
      .build();

    camundaWriteableAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_WRITEABLE)
      .namespace(CAMUNDA_NS)
      .defaultValue(true)
      .build();

    camundaVariableAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDatePatternAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DATE_PATTERN)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDefaultAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DEFAULT)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaValueCollection = sequenceBuilder.elementCollection(CamundaValue.class)
      .build();

    typeBuilder.build();
  }

  public CamundaFormPropertyImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaId() {
    return camundaIdAttribute.getValue(this);
  }

  public void setCamundaId(String camundaId) {
    camundaIdAttribute.setValue(this, camundaId);
  }

  public String getCamundaName() {
    return camundaNameAttribute.getValue(this);
  }

  public void setCamundaName(String camundaName) {
    camundaNameAttribute.setValue(this, camundaName);
  }

  public String getCamundaType() {
    return camundaTypeAttribute.getValue(this);
  }

  public void setCamundaType(String camundaType) {
    camundaTypeAttribute.setValue(this, camundaType);
  }

  public boolean isCamundaRequired() {
    return camundaRequiredAttribute.getValue(this);
  }

  public void setCamundaRequired(boolean isCamundaRequired) {
    camundaRequiredAttribute.setValue(this, isCamundaRequired);
  }

  public boolean isCamundaReadable() {
    return camundaReadableAttribute.getValue(this);
  }

  public void setCamundaReadable(boolean isCamundaReadable) {
    camundaReadableAttribute.setValue(this, isCamundaReadable);
  }

  public boolean isCamundaWriteable() {
    return camundaWriteableAttribute.getValue(this);
  }

  public void setCamundaWriteable(boolean isCamundaWriteable) {
    camundaWriteableAttribute.setValue(this, isCamundaWriteable);
  }

  public String getCamundaVariable() {
    return camundaVariableAttribute.getValue(this);
  }

  public void setCamundaVariable(String camundaVariable) {
    camundaVariableAttribute.setValue(this, camundaVariable);
  }

  public String getCamundaExpression() {
    return camundaExpressionAttribute.getValue(this);
  }

  public void setCamundaExpression(String camundaExpression) {
    camundaExpressionAttribute.setValue(this, camundaExpression);
  }

  public String getCamundaDatePattern() {
    return camundaDatePatternAttribute.getValue(this);
  }

  public void setCamundaDatePattern(String camundaDatePattern) {
    camundaDatePatternAttribute.setValue(this, camundaDatePattern);
  }

  public String getCamundaDefault() {
    return camundaDefaultAttribute.getValue(this);
  }

  public void setCamundaDefault(String camundaDefault) {
    camundaDefaultAttribute.setValue(this, camundaDefault);
  }

  public Collection<CamundaValue> getCamundaValues() {
    return camundaValueCollection.get(this);
  }
}
