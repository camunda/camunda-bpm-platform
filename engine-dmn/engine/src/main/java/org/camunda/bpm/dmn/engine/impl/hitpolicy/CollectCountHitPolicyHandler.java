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

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

import java.util.List;

public class CollectCountHitPolicyHandler extends AbstractCollectNumberHitPolicyHandler {

  protected static final HitPolicyEntry HIT_POLICY = new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.COUNT);

  @Override
  public HitPolicyEntry getHitPolicyEntry() {
    return HIT_POLICY;
  }

  @Override
  protected BuiltinAggregator getAggregator() {
    return BuiltinAggregator.COUNT;
  }

  @Override
  protected TypedValue aggregateValues(List<TypedValue> values) {
    return Variables.integerValue(values.size());
  }

  @Override
  protected Integer aggregateIntegerValues(List<Integer> intValues) {
    // not used
    return 0;
  }

  @Override
  protected Long aggregateLongValues(List<Long> longValues) {
    // not used
    return 0L;
  }

  @Override
  protected Double aggregateDoubleValues(List<Double> doubleValues) {
    // not used
    return 0.0;
  }

  @Override
  public String toString() {
    return "CollectCountHitPolicyHandler{}";
  }

}
