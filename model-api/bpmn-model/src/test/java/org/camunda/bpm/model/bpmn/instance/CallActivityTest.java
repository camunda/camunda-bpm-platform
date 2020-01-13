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
public class CallActivityTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(Activity.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("calledElement"),
      /** camunda extensions */
      new AttributeAssumption(CAMUNDA_NS, "async", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "calledElementBinding"),
      new AttributeAssumption(CAMUNDA_NS, "calledElementVersion"),
      new AttributeAssumption(CAMUNDA_NS, "calledElementVersionTag"),
      new AttributeAssumption(CAMUNDA_NS, "calledElementTenantId"),
      new AttributeAssumption(CAMUNDA_NS, "caseRef"),
      new AttributeAssumption(CAMUNDA_NS, "caseBinding"),
      new AttributeAssumption(CAMUNDA_NS, "caseVersion"),
      new AttributeAssumption(CAMUNDA_NS, "caseTenantId"),
      new AttributeAssumption(CAMUNDA_NS, "variableMappingClass"),
      new AttributeAssumption(CAMUNDA_NS, "variableMappingDelegateExpression")
    );
  }
}
