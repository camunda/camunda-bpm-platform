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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PLAN_ITEM_CONTROL;

import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.ManualActivationRule;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.RepetitionRule;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class PlanItemControlImpl extends CmmnElementImpl implements PlanItemControl {

  protected static ChildElement<RepetitionRule> repetitionRuleChild;
  protected static ChildElement<RequiredRule> requiredRuleChild;
  protected static ChildElement<ManualActivationRule> manualActivationRuleChild;

  public PlanItemControlImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public RepetitionRule getRepetitionRule() {
    return repetitionRuleChild.getChild(this);
  }

  public void setRepetitionRule(RepetitionRule repetitionRule) {
    repetitionRuleChild.setChild(this, repetitionRule);
  }

  public RequiredRule getRequiredRule() {
    return requiredRuleChild.getChild(this);
  }

  public void setRequiredRule(RequiredRule requiredRule) {
    requiredRuleChild.setChild(this, requiredRule);
  }

  public ManualActivationRule getManualActivationRule() {
    return manualActivationRuleChild.getChild(this);
  }

  public void setManualActivationRule(ManualActivationRule manualActivationRule) {
    manualActivationRuleChild.setChild(this, manualActivationRule);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PlanItemControl.class, CMMN_ELEMENT_PLAN_ITEM_CONTROL)
        .namespaceUri(CMMN11_NS)
        .extendsType(CmmnElement.class)
        .instanceProvider(new ModelTypeInstanceProvider<PlanItemControl>() {
          public PlanItemControl newInstance(ModelTypeInstanceContext instanceContext) {
            return new PlanItemControlImpl(instanceContext);
          }
        });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    repetitionRuleChild = sequenceBuilder.element(RepetitionRule.class)
        .build();

    requiredRuleChild = sequenceBuilder.element(RequiredRule.class)
        .build();

    manualActivationRuleChild = sequenceBuilder.element(ManualActivationRule.class)
        .build();

    typeBuilder.build();
  }

}
