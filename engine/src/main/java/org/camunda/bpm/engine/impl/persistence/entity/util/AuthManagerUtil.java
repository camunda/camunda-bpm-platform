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
package org.camunda.bpm.engine.impl.persistence.entity.util;

import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;

public class AuthManagerUtil {

  public static VariablePermissions getVariablePermissions(boolean ensureSpecificVariablePermission) {
    return new VariablePermissions(ensureSpecificVariablePermission);
  }

  public static class VariablePermissions {

    protected Permission processDefinitionPermission;
    protected Permission historicTaskPermission;

    public VariablePermissions(boolean ensureSpecificVariablePermission) {
      if (ensureSpecificVariablePermission) {
        processDefinitionPermission = ProcessDefinitionPermissions.READ_HISTORY_VARIABLE;
        historicTaskPermission = HistoricTaskPermissions.READ_VARIABLE;

      } else {
        processDefinitionPermission = ProcessDefinitionPermissions.READ_HISTORY;
        historicTaskPermission = HistoricTaskPermissions.READ;

      }
    }

    public Permission getProcessDefinitionPermission() {
      return processDefinitionPermission;
    }

    public Permission getHistoricTaskPermission() {
      return historicTaskPermission;
    }
  }

}
