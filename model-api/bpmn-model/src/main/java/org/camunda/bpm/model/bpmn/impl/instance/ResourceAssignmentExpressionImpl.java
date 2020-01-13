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

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Expression;
import org.camunda.bpm.model.bpmn.instance.ResourceAssignmentExpression;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_RESOURCE_ASSIGNMENT_EXPRESSION;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN resourceAssignmentExpression element
 *
 * @author Sebastian Menski
 */
public class ResourceAssignmentExpressionImpl extends BaseElementImpl implements ResourceAssignmentExpression {

  protected static ChildElement<Expression> expressionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ResourceAssignmentExpression.class, BPMN_ELEMENT_RESOURCE_ASSIGNMENT_EXPRESSION)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ResourceAssignmentExpression>() {
        public ResourceAssignmentExpression newInstance(ModelTypeInstanceContext instanceContext) {
          return new ResourceAssignmentExpressionImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    expressionChild = sequenceBuilder.element(Expression.class)
      .required()
      .build();

    typeBuilder.build();
  }

  public ResourceAssignmentExpressionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Expression getExpression() {
    return expressionChild.getChild(this);
  }

  public void setExpression(Expression expression) {
    expressionChild.setChild(this, expression);
  }
}
