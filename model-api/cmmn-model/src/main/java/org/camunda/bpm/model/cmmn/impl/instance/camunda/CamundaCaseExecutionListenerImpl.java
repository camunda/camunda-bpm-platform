/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.model.cmmn.impl.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_EVENT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_CASE_EXECUTION_LISTENER;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class CamundaCaseExecutionListenerImpl extends CmmnModelElementInstanceImpl implements CamundaCaseExecutionListener {

  protected static Attribute<String> camundaEventAttribute;
  protected static Attribute<String> camundaClassAttribute;
  protected static Attribute<String> camundaExpressionAttribute;
  protected static Attribute<String> camundaDelegateExpressionAttribute;
  protected static ChildElementCollection<CamundaField> camundaFieldCollection;
  protected static ChildElement<CamundaScript> camundaScriptChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaCaseExecutionListener.class, CAMUNDA_ELEMENT_CASE_EXECUTION_LISTENER)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaCaseExecutionListener>() {
        public CamundaCaseExecutionListener newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaCaseExecutionListenerImpl(instanceContext);
        }
      });

    camundaEventAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_EVENT)
      .namespace(CAMUNDA_NS)
      .build();

    camundaClassAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CLASS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDelegateExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaFieldCollection = sequenceBuilder.elementCollection(CamundaField.class)
      .build();

    camundaScriptChild = sequenceBuilder.element(CamundaScript.class)
        .build();

    typeBuilder.build();
  }

  public CamundaCaseExecutionListenerImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaEvent() {
    return camundaEventAttribute.getValue(this);
  }

  public void setCamundaEvent(String camundaEvent) {
    camundaEventAttribute.setValue(this, camundaEvent);
  }

  public String getCamundaClass() {
    return camundaClassAttribute.getValue(this);
  }

  public void setCamundaClass(String camundaClass) {
    camundaClassAttribute.setValue(this, camundaClass);
  }

  public String getCamundaExpression() {
    return camundaExpressionAttribute.getValue(this);
  }

  public void setCamundaExpression(String camundaExpression) {
    camundaExpressionAttribute.setValue(this, camundaExpression);
  }

  public String getCamundaDelegateExpression() {
    return camundaDelegateExpressionAttribute.getValue(this);
  }

  public void setCamundaDelegateExpression(String camundaDelegateExpression) {
    camundaDelegateExpressionAttribute.setValue(this, camundaDelegateExpression);
  }

  public CamundaScript getCamundaScript() {
    return camundaScriptChild.getChild(this);
  }

  public void setCamundaScript(CamundaScript camundaScript) {
    camundaScriptChild.setChild(this, camundaScript);
  }

  public Collection<CamundaField> getCamundaFields() {
    return camundaFieldCollection.get(this);
  }

}
