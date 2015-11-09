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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DECISION_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_DECISION_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.DecisionRefExpression;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.ParameterMapping;
import org.camunda.bpm.model.cmmn.instance.Task;
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
public class DecisionTaskImpl extends TaskImpl implements DecisionTask {

  protected static Attribute<String> decisionRefAttribute;

  protected static ChildElementCollection<ParameterMapping> parameterMappingCollection;
  protected static ChildElement<DecisionRefExpression> decisionRefExpressionChild;

  public DecisionTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getDecision() {
    return decisionRefAttribute.getValue(this);
  }

  public void setDecision(String process) {
    decisionRefAttribute.setValue(this, process);
  }

  public Collection<ParameterMapping> getParameterMappings() {
    return parameterMappingCollection.get(this);
  }

  public DecisionRefExpression getDecisionExpression() {
    return decisionRefExpressionChild.getChild(this);
  }

  public void setDecisionExpression(DecisionRefExpression decisionExpression) {
    decisionRefExpressionChild.setChild(this, decisionExpression);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DecisionTask.class, CMMN_ELEMENT_DECISION_TASK)
        .namespaceUri(CMMN11_NS)
        .extendsType(Task.class)
        .instanceProvider(new ModelTypeInstanceProvider<DecisionTask>() {
          public DecisionTask newInstance(ModelTypeInstanceContext instanceContext) {
            return new DecisionTaskImpl(instanceContext);
          }
        });

    decisionRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_DECISION_REF)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterMappingCollection = sequenceBuilder.elementCollection(ParameterMapping.class)
        .build();

    decisionRefExpressionChild = sequenceBuilder.element(DecisionRefExpression.class)
        .build();

    typeBuilder.build();
  }

}
