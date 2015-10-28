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

package org.camunda.bpm.model.dmn.impl.instance;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN11_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_BINDING;

import org.camunda.bpm.model.dmn.instance.Binding;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.InformationItem;
import org.camunda.bpm.model.dmn.instance.ParameterReference;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReference;

public class BindingImpl extends DmnModelElementInstanceImpl implements Binding {
  
  protected static ChildElement<Expression> expressionChild;
  protected static ElementReference<InformationItem, ParameterReference> parameterRef;
  
  public BindingImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Expression getExpression() {
    return expressionChild.getChild(this);
  }

  public void setExpression(Expression expression) {
    expressionChild.setChild(this, expression);
  }

  public InformationItem getParameter() {
    return parameterRef.getReferenceTargetElement(this);
  }

  public void setParameter(InformationItem parameter) {
    parameterRef.setReferenceTargetElement(this, parameter);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Binding.class, DMN_ELEMENT_BINDING)
      .namespaceUri(DMN11_NS)
      .instanceProvider(new ModelTypeInstanceProvider<Binding>() {
        public Binding newInstance(ModelTypeInstanceContext instanceContext) {
          return new BindingImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    expressionChild = sequenceBuilder.element(Expression.class)
      .build();

    parameterRef = sequenceBuilder.element(ParameterReference.class)
      .required()
      .uriElementReference(InformationItem.class)
      .build();

    typeBuilder.build();
  }

}
