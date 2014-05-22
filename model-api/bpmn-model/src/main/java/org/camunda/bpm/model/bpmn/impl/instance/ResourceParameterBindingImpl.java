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

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Expression;
import org.camunda.bpm.model.bpmn.instance.ResourceParameter;
import org.camunda.bpm.model.bpmn.instance.ResourceParameterBinding;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN resourceParameterBinding element
 *
 * @author Sebastian Menski
 */
public class ResourceParameterBindingImpl extends BaseElementImpl implements ResourceParameterBinding {

  protected static AttributeReference<ResourceParameter> parameterRefAttribute;
  protected static ChildElement<Expression> expressionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ResourceParameterBinding.class, BPMN_ELEMENT_RESOURCE_PARAMETER_BINDING)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ResourceParameterBinding>() {
        public ResourceParameterBinding newInstance(ModelTypeInstanceContext instanceContext) {
          return new ResourceParameterBindingImpl(instanceContext);
        }
      });

    parameterRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_PARAMETER_REF)
      .required()
      .qNameAttributeReference(ResourceParameter.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    expressionChild = sequenceBuilder.element(Expression.class)
      .required()
      .build();

    typeBuilder.build();
  }

  public ResourceParameterBindingImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public ResourceParameter getParameter() {
    return parameterRefAttribute.getReferenceTargetElement(this);
  }

  public void setParameter(ResourceParameter parameter) {
    parameterRefAttribute.setReferenceTargetElement(this, parameter);
  }

  public Expression getExpression() {
    return expressionChild.getChild(this);
  }

  public void setExpression(Expression expression) {
    expressionChild.setChild(this, expression);
  }
}
