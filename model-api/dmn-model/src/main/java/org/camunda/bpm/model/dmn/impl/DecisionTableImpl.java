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

package org.camunda.bpm.model.dmn.impl;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN10_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_AGGREGATION;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_HIT_POLICY;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_IS_COMPLETE;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_IS_CONSISTENT;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_PREFERED_ORIENTATION;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_DECISION_TABLE;

import java.util.Collection;

import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.DecisionTableOrientation;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

public class DecisionTableImpl extends ExpressionImpl implements DecisionTable {
  
  protected static Attribute<HitPolicy> hitPolicyAttribute;
  protected static Attribute<BuiltinAggregator> aggregationAttribute;
  protected static Attribute<DecisionTableOrientation> preferedOrientationAttribute;
  protected static Attribute<Boolean> isCompleteAttribute;
  protected static Attribute<Boolean> isConsistentAttribute;

  protected static ChildElementCollection<Clause> clauseCollection;
  protected static ChildElementCollection<Rule> ruleCollection;

  public DecisionTableImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public HitPolicy getHitPolicy() {
    return hitPolicyAttribute.getValue(this);
  }

  public void setHitPolicy(HitPolicy hitPolicy) {
    hitPolicyAttribute.setValue(this, hitPolicy);
  }

  public BuiltinAggregator getAggregation() {
    return aggregationAttribute.getValue(this);
  }

  public void setAggregation(BuiltinAggregator aggregation) {
    aggregationAttribute.setValue(this, aggregation);
  }

  public DecisionTableOrientation getPreferedOrientation() {
    return preferedOrientationAttribute.getValue(this);
  }

  public void setPreferedOrientation(DecisionTableOrientation preferedOrientation) {
    preferedOrientationAttribute.setValue(this, preferedOrientation);
  }

  public boolean isComplete() {
    return isCompleteAttribute.getValue(this);
  }

  public void setComplete(boolean isComplete) {
    isCompleteAttribute.setValue(this, isComplete);
  }

  public boolean isConsistent() {
    return isConsistentAttribute.getValue(this);
  }

  public void setConsistent(boolean isConsistent) {
    isConsistentAttribute.setValue(this, isConsistent);
  }

  public Collection<Clause> getClauses() {
    return clauseCollection.get(this);
  }

  public Collection<Rule> getRules() {
    return ruleCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DecisionTable.class, DMN_ELEMENT_DECISION_TABLE)
      .namespaceUri(DMN10_NS)
      .extendsType(Expression.class)
      .instanceProvider(new ModelTypeInstanceProvider<DecisionTable>() {
        public DecisionTable newInstance(ModelTypeInstanceContext instanceContext) {
          return new DecisionTableImpl(instanceContext);
        }
      });

    hitPolicyAttribute = typeBuilder.namedEnumAttribute(DMN_ATTRIBUTE_HIT_POLICY, HitPolicy.class)
      .defaultValue(HitPolicy.UNIQUE)
      .build();

    aggregationAttribute = typeBuilder.enumAttribute(DMN_ATTRIBUTE_AGGREGATION, BuiltinAggregator.class)
      .build();

    preferedOrientationAttribute = typeBuilder.namedEnumAttribute(DMN_ATTRIBUTE_PREFERED_ORIENTATION, DecisionTableOrientation.class)
      .defaultValue(DecisionTableOrientation.Rule_as_Row)
      .build();

    isCompleteAttribute = typeBuilder.booleanAttribute(DMN_ATTRIBUTE_IS_COMPLETE)
      .defaultValue(false)
      .build();

    isConsistentAttribute = typeBuilder.booleanAttribute(DMN_ATTRIBUTE_IS_CONSISTENT)
      .defaultValue(false)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    clauseCollection = sequenceBuilder.elementCollection(Clause.class)
      .build();

    ruleCollection = sequenceBuilder.elementCollection(Rule.class)
      .build();

    typeBuilder.build();
  }

}
