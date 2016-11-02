/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

/**
 * @author Askar Akhmerov
 */
public class HitPolicyEntry {

  protected final HitPolicy hitPolicy;
  protected final BuiltinAggregator builtinAggregator;

  public HitPolicyEntry(HitPolicy hitPolicy, BuiltinAggregator builtinAggregator) {
    this.hitPolicy = hitPolicy;
    this.builtinAggregator = builtinAggregator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HitPolicyEntry that = (HitPolicyEntry) o;

    if (hitPolicy != that.hitPolicy) return false;
    return builtinAggregator == that.builtinAggregator;

  }

  @Override
  public int hashCode() {
    int result = hitPolicy != null ? hitPolicy.hashCode() : 0;
    result = 31 * result + (builtinAggregator != null ? builtinAggregator.hashCode() : 0);
    return result;
  }

}
