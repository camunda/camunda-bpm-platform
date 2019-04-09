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

import java.util.List;

import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class CollectSumHitPolicyHandler extends AbstractCollectNumberHitPolicyHandler {
  protected static final HitPolicyEntry HIT_POLICY = new HitPolicyEntry(HitPolicy.COLLECT, BuiltinAggregator.SUM);

  @Override
  public HitPolicyEntry getHitPolicyEntry() {
    return HIT_POLICY;
  }

  protected BuiltinAggregator getAggregator() {
    return BuiltinAggregator.SUM;
  }

  @Override
  protected Integer aggregateIntegerValues(List<Integer> intValues) {
    int sum = 0;
    for (Integer intValue : intValues) {
      if (intValue != null) {
        sum += intValue;
      }
    }
    return sum;
  }

  @Override
  protected Long aggregateLongValues(List<Long> longValues) {
    long sum = 0L;
    for (Long longValue : longValues) {
      if (longValue != null) {
        sum += longValue;
      }
    }
    return sum;
  }

  @Override
  protected Double aggregateDoubleValues(List<Double> doubleValues) {
    double sum = 0.0;
    for (Double doubleValue : doubleValues) {
      if (doubleValue != null) {
        sum += doubleValue;
      }
    }
    return sum;
  }

  @Override
  public String toString() {
    return "CollectSumHitPolicyHandler{}";
  }

}
