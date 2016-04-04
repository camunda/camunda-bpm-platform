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

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_DECISION_BINDING;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_DECISION_VERSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_DECISION_TENANT_ID;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_MAP_DECISION_RESULT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_RESULT_VARIABLE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;
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

  /** Camunda extensions */
  protected static Attribute<String> camundaResultVariableAttribute;
  protected static Attribute<String> camundaDecisionBindingAttribute;
  protected static Attribute<String> camundaDecisionVersionAttribute;
  protected static Attribute<String> camundaDecisionTenantIdAttribute;
  protected static Attribute<String> camundaMapDecisionResultAttribute;

  public DecisionTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getDecision() {
    return decisionRefAttribute.getValue(this);
  }

  public void setDecision(String decision) {
    decisionRefAttribute.setValue(this, decision);
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

  public String getCamundaResultVariable() {
    return camundaResultVariableAttribute.getValue(this);
  }

  public void setCamundaResultVariable(String camundaResultVariable) {
    camundaResultVariableAttribute.setValue(this, camundaResultVariable);
  }

  public String getCamundaDecisionBinding() {
    return camundaDecisionBindingAttribute.getValue(this);
  }

  public void setCamundaDecisionBinding(String camundaDecisionBinding) {
    camundaDecisionBindingAttribute.setValue(this, camundaDecisionBinding);
  }

  public String getCamundaDecisionVersion() {
    return camundaDecisionVersionAttribute.getValue(this);
  }

  public void setCamundaDecisionVersion(String camundaDecisionVersion) {
    camundaDecisionVersionAttribute.setValue(this, camundaDecisionVersion);
  }

  public String getCamundaDecisionTenantId() {
    return camundaDecisionTenantIdAttribute.getValue(this);
  }

  public void setCamundaDecisionTenantId(String camundaDecisionTenantId) {
    camundaDecisionTenantIdAttribute.setValue(this, camundaDecisionTenantId);
  }

  @Override
  public String getCamundaMapDecisionResult() {
    return camundaMapDecisionResultAttribute.getValue(this);
  }

  @Override
  public void setCamundaMapDecisionResult(String camundaMapDecisionResult) {
    camundaMapDecisionResultAttribute.setValue(this, camundaMapDecisionResult);
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

    /** Camunda extensions */

    camundaResultVariableAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_RESULT_VARIABLE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDecisionBindingAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DECISION_BINDING)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDecisionVersionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DECISION_VERSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDecisionTenantIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DECISION_TENANT_ID)
        .namespace(CAMUNDA_NS)
        .build();

    camundaMapDecisionResultAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_MAP_DECISION_RESULT)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterMappingCollection = sequenceBuilder.elementCollection(ParameterMapping.class)
        .build();

    decisionRefExpressionChild = sequenceBuilder.element(DecisionRefExpression.class)
        .build();

    typeBuilder.build();
  }

}
