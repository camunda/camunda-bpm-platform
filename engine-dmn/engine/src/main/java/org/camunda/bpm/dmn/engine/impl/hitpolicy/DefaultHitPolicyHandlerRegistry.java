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
package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandlerRegistry;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DefaultHitPolicyHandlerRegistry implements DmnHitPolicyHandlerRegistry {

  protected static final Map<HitPolicyEntry, DmnHitPolicyHandler> handlers = getDefaultHandlers();

  protected static Map<HitPolicyEntry, DmnHitPolicyHandler> getDefaultHandlers() {
    Map<HitPolicyEntry, DmnHitPolicyHandler> handlers = new HashMap<HitPolicyEntry, DmnHitPolicyHandler>();

    handlers.put(new HitPolicyEntry(HitPolicy.UNIQUE, null), new UniqueHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.FIRST, null), new FirstHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.ANY, null), new AnyHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.RULE_ORDER, null), new RuleOrderHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.COLLECT, null), new CollectHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.COUNT), new CollectCountHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.SUM), new CollectSumHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.MIN), new CollectMinHitPolicyHandler());
    handlers.put(new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.MAX), new CollectMaxHitPolicyHandler());

    return handlers;
  }

  public DmnHitPolicyHandler getHandler(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator) {
    return handlers.get(new HitPolicyEntry(hitPolicy, builtinAggregator));
  }

  public void addHandler(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator, DmnHitPolicyHandler hitPolicyHandler) {
    handlers.put(new HitPolicyEntry(hitPolicy, builtinAggregator), hitPolicyHandler);
  }

}
