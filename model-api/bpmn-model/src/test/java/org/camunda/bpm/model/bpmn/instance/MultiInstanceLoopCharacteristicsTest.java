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
package org.camunda.bpm.model.bpmn.instance;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.bpmn.MultiInstanceFlowCondition;
import org.camunda.bpm.model.bpmn.impl.instance.LoopDataInputRef;
import org.camunda.bpm.model.bpmn.impl.instance.LoopDataOutputRef;

/**
 * @author Filip Hrisafov
 */
public class MultiInstanceLoopCharacteristicsTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(LoopCharacteristics.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(LoopCardinality.class, 0, 1),
      new ChildElementAssumption(LoopDataInputRef.class, 0, 1),
      new ChildElementAssumption(LoopDataOutputRef.class, 0, 1),
      new ChildElementAssumption(OutputDataItem.class, 0, 1),
      new ChildElementAssumption(InputDataItem.class, 0, 1),
      new ChildElementAssumption(ComplexBehaviorDefinition.class),
      new ChildElementAssumption(CompletionCondition.class, 0, 1)
    );
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("isSequential", false, false, false),
      new AttributeAssumption("behavior", false, false, MultiInstanceFlowCondition.All),
      new AttributeAssumption("oneBehaviorEventRef"),
      new AttributeAssumption("noneBehaviorEventRef"),
      new AttributeAssumption(CAMUNDA_NS, "asyncBefore", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "asyncAfter", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "exclusive", false, false, true),
      new AttributeAssumption(CAMUNDA_NS, "collection"),
      new AttributeAssumption(CAMUNDA_NS, "elementVariable")
    );
  }
}
