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
package org.camunda.bpm.engine.history;


public interface ExternalTaskState {

  ExternalTaskState CREATED = new ExternalTaskStateImpl(0, "created");
  ExternalTaskState FAILED = new ExternalTaskStateImpl(1, "failed");
  ExternalTaskState SUCCESSFUL = new ExternalTaskStateImpl(2, "successful");
  ExternalTaskState DELETED = new ExternalTaskStateImpl(3, "deleted");

  int getStateCode();

  ///////////////////////////////////////////////////// default implementation

  static class ExternalTaskStateImpl implements ExternalTaskState {

    public final int stateCode;
    protected final String name;

    public ExternalTaskStateImpl(int stateCode, String string) {
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
      ExternalTaskStateImpl other = (ExternalTaskStateImpl) obj;
      return stateCode == other.stateCode;
    }

    @Override
    public String toString() {
      return name;
    }
  }

}
