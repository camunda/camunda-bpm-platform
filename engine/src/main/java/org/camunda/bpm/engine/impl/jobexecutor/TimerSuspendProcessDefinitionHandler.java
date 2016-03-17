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
package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.cmd.AbstractSetProcessDefinitionStateCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Joram Barrez
 * @author roman.smirnov
 */
public class TimerSuspendProcessDefinitionHandler extends TimerChangeProcessDefinitionSuspensionStateJobHandler {

  public static final String TYPE = "suspend-processdefinition";

  public String getType() {
    return TYPE;
  }

  protected AbstractSetProcessDefinitionStateCmd getCommand(String configuration) {
    JSONObject config = new JSONObject(configuration);

    boolean activateProcessInstances = getIncludeProcessInstances(config);

    SuspendProcessDefinitionCmd cmd = null;

    String by = getBy(config);

    if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID)) {
      String processDefinitionId = getProcessDefinitionId(config);
      cmd = new SuspendProcessDefinitionCmd(processDefinitionId, null, null, activateProcessInstances, null);
    } else

    if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY)) {
      String processDefinitionKey = getProcessDefinitionKey(config);
      cmd = new SuspendProcessDefinitionCmd(null, processDefinitionKey, null, activateProcessInstances, null);
    }

    return cmd;
  }

}
