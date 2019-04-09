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

import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

/**
 * Represents the hit policy and the aggregator of a decision table.
 *
 * @author Askar Akhmerov
 */
public class HitPolicyEntry {

  protected final HitPolicy hitPolicy;
  protected final BuiltinAggregator aggregator;

  public HitPolicyEntry(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator) {
    this.hitPolicy = hitPolicy;
    this.aggregator = builtinAggregator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HitPolicyEntry that = (HitPolicyEntry) o;

    if (hitPolicy != that.hitPolicy) return false;
    return aggregator == that.aggregator;

  }

  @Override
  public int hashCode() {
    int result = hitPolicy != null ? hitPolicy.hashCode() : 0;
    result = 31 * result + (aggregator != null ? aggregator.hashCode() : 0);
    return result;
  }

  public HitPolicy getHitPolicy() {
    return hitPolicy;
  }

  public BuiltinAggregator getAggregator() {
    return aggregator;
  }

}
