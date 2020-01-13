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

/**
 * @author Sebastian Menski
 */
public class BusinessRuleTaskTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(Task.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("implementation", false, false, "##unspecified"),
      /** camunda extensions */
      new AttributeAssumption(CAMUNDA_NS, "class"),
      new AttributeAssumption(CAMUNDA_NS, "delegateExpression"),
      new AttributeAssumption(CAMUNDA_NS, "expression"),
      new AttributeAssumption(CAMUNDA_NS, "resultVariable"),
      new AttributeAssumption(CAMUNDA_NS, "topic"),
      new AttributeAssumption(CAMUNDA_NS, "type"),
      new AttributeAssumption(CAMUNDA_NS, "decisionRef"),
      new AttributeAssumption(CAMUNDA_NS, "decisionRefBinding"),
      new AttributeAssumption(CAMUNDA_NS, "decisionRefVersion"),
      new AttributeAssumption(CAMUNDA_NS, "decisionRefVersionTag"),
      new AttributeAssumption(CAMUNDA_NS, "decisionRefTenantId"),
      new AttributeAssumption(CAMUNDA_NS, "mapDecisionResult"),
      new AttributeAssumption(CAMUNDA_NS, "taskPriority")
    );
  }

}
