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
public interface CaseExecutionState {

  CaseExecutionState AVAILABLE = new CaseExecutionStateImpl(1, "available");
  CaseExecutionState ENABLED = new CaseExecutionStateImpl(2, "enabled");
  CaseExecutionState DISABLED = new CaseExecutionStateImpl(3, "disabled");
  CaseExecutionState ACTIVE = new CaseExecutionStateImpl(4, "active");
  CaseExecutionState SUSPENDED = new CaseExecutionStateImpl(5, "suspended");
  CaseExecutionState TERMINATED = new CaseExecutionStateImpl(6, "terminated");
  CaseExecutionState COMPLETED = new CaseExecutionStateImpl(7, "completed");
  CaseExecutionState FAILED = new CaseExecutionStateImpl(8, "failed");
  CaseExecutionState CLOSED = new CaseExecutionStateImpl(9, "closed");

  int getStateCode();

  ///////////////////////////////////////////////////// default implementation

  static class CaseExecutionStateImpl implements CaseExecutionState {

    public final int stateCode;
    protected final String name;

    public CaseExecutionStateImpl(int stateCode, String string) {
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
      CaseExecutionStateImpl other = (CaseExecutionStateImpl) obj;
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
