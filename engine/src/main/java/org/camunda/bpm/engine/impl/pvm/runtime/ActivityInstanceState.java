/*
 * Copyright 2014 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.impl.pvm.runtime;

/**
 * Contains a predefined set of states activity instances may be in
 * during the execution of a process instance.
 *
 * @author nico.rehwaldt
 */
public interface ActivityInstanceState {

  ActivityInstanceState DEFAULT = new ActivityInstanceStateImpl(0, "default");
  ActivityInstanceState SCOPE_COMPLETE = new ActivityInstanceStateImpl(1, "scopeComplete");
  ActivityInstanceState CANCELED = new ActivityInstanceStateImpl(2, "canceled");
  ActivityInstanceState STARTING = new ActivityInstanceStateImpl(3, "starting");
  ActivityInstanceState ENDING = new ActivityInstanceStateImpl(4, "ending");

  int getStateCode();

  ///////////////////////////////////////////////////// default implementation

  static class ActivityInstanceStateImpl implements ActivityInstanceState {

    public final int stateCode;
    protected final String name;

    public ActivityInstanceStateImpl(int suspensionCode, String string) {
      this.stateCode = suspensionCode;
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
      ActivityInstanceStateImpl other = (ActivityInstanceStateImpl) obj;
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
