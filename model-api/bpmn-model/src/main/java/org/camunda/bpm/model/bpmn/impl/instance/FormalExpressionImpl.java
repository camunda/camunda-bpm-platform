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
package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.instance.Expression;
import org.camunda.bpm.model.bpmn.instance.FormalExpression;
import org.camunda.bpm.model.bpmn.instance.ItemDefinition;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN formalExpression element
 *
 * @author Sebastian Menski
 */
public class FormalExpressionImpl extends ExpressionImpl implements FormalExpression {

  protected static Attribute<String> languageAttribute;
  protected static AttributeReference<ItemDefinition> evaluatesToTypeRefAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(FormalExpression.class, BPMN_ELEMENT_FORMAL_EXPRESSION)
      .namespaceUri(BPMN20_NS)
      .extendsType(Expression.class)
      .instanceProvider(new ModelTypeInstanceProvider<FormalExpression>() {
        public FormalExpression newInstance(ModelTypeInstanceContext instanceContext) {
          return new FormalExpressionImpl(instanceContext);
        }
      });

    languageAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_LANGUAGE)
      .build();

    evaluatesToTypeRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_EVALUATES_TO_TYPE_REF)
      .qNameAttributeReference(ItemDefinition.class)
      .build();

    typeBuilder.build();
  }

  public FormalExpressionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getLanguage() {
    return languageAttribute.getValue(this);
  }

  public void setLanguage(String language) {
    languageAttribute.setValue(this, language);
  }

  public ItemDefinition getEvaluatesToType() {
    return evaluatesToTypeRefAttribute.getReferenceTargetElement(this);
  }

  public void setEvaluatesToType(ItemDefinition evaluatesToType) {
    evaluatesToTypeRefAttribute.setReferenceTargetElement(this, evaluatesToType);
  }
}
