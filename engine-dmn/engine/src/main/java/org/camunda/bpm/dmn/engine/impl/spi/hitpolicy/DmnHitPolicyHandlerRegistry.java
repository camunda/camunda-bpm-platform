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
package org.camunda.bpm.dmn.engine.impl.spi.hitpolicy;

import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

/**
 * Registry of hit policy handlers
 */
public interface DmnHitPolicyHandlerRegistry {

  /**
   * Get a hit policy for a {@link HitPolicy} and {@link BuiltinAggregator} combination.
   *
   * @param hitPolicy the hit policy
   * @param builtinAggregator the aggregator or null if not required
   * @return the handler which is registered for this hit policy, or null if none exist
   */
  DmnHitPolicyHandler getHandler(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator);

  /**
   * Register a hit policy handler for a {@link HitPolicy} and {@link BuiltinAggregator} combination.
   *
   * @param hitPolicy the hit policy
   * @param builtinAggregator the aggregator or null if not required
   * @param hitPolicyHandler the hit policy handler to registry
   */
  void addHandler(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator, DmnHitPolicyHandler hitPolicyHandler);

}
