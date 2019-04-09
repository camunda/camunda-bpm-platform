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
package org.camunda.bpm.dmn.engine.impl.transform;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;

public class DmnDecisionTableTransformHandler implements DmnElementTransformHandler<DecisionTable, DmnDecisionTableImpl> {

  protected static final DmnTransformLogger LOG = DmnLogger.TRANSFORM_LOGGER;

  public DmnDecisionTableImpl handleElement(DmnElementTransformContext context, DecisionTable decisionTable) {
    return createFromDecisionTable(context, decisionTable);
  }

  protected DmnDecisionTableImpl createFromDecisionTable(DmnElementTransformContext context, DecisionTable decisionTable) {
    DmnDecisionTableImpl dmnDecisionTable = createDmnElement(context, decisionTable);

    dmnDecisionTable.setHitPolicyHandler(getHitPolicyHandler(context, decisionTable, dmnDecisionTable));

    return dmnDecisionTable;
  }

  protected DmnDecisionTableImpl createDmnElement(DmnElementTransformContext context, DecisionTable decisionTable) {
    return new DmnDecisionTableImpl();
  }

  protected DmnHitPolicyHandler getHitPolicyHandler(DmnElementTransformContext context, DecisionTable decisionTable, DmnDecisionTableImpl dmnDecisionTable) {
    HitPolicy hitPolicy = decisionTable.getHitPolicy();
    if (hitPolicy == null) {
      // use default hit policy
      hitPolicy = HitPolicy.UNIQUE;
    }
    BuiltinAggregator aggregation = decisionTable.getAggregation();
    DmnHitPolicyHandler hitPolicyHandler = context.getHitPolicyHandlerRegistry().getHandler(hitPolicy, aggregation);
    if (hitPolicyHandler != null) {
      return hitPolicyHandler;
    }
    else {
      throw LOG.hitPolicyNotSupported(dmnDecisionTable, hitPolicy, aggregation);
    }
  }

}
