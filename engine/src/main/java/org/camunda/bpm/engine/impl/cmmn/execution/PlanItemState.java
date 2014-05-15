/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmmn.execution;



/**
 * @author Roman Smirnov
 *
 */
public interface PlanItemState {

  PlanItemState AVAILABLE = new PlanItemStateImpl(1, "available");
  PlanItemState ENABLED = new PlanItemStateImpl(2, "enabled");
  PlanItemState DISABLED = new PlanItemStateImpl(3, "disabled");
  PlanItemState ACTIVE = new PlanItemStateImpl(4, "active");
  PlanItemState SUSPENDED = new PlanItemStateImpl(5, "suspended");
  PlanItemState TERMINATED = new PlanItemStateImpl(6, "terminated");
  PlanItemState COMPLETED = new PlanItemStateImpl(7, "completed");
  PlanItemState FAILED = new PlanItemStateImpl(8, "failed");
  PlanItemState CLOSED = new PlanItemStateImpl(9, "closed");

  int getStateCode();

  ///////////////////////////////////////////////////// default implementation

  static class PlanItemStateImpl implements PlanItemState {

    public final int stateCode;
    protected final String name;

    public PlanItemStateImpl(int stateCode, String string) {
      this.stateCode = stateCode;
      this.name = string;
    }

    public int getStateCode() {
      return stateCode;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + stateCode;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      PlanItemStateImpl other = (PlanItemStateImpl) obj;
      if (stateCode != other.stateCode)
        return false;
      return true;
    }

    @Override
    public String toString() {
      return name;
    }
  }

}
