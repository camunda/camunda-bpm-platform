/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNumberOfElements;

/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstancesCmd implements Command<Void>, Serializable {

  protected final List<String> processInstanceIds;

  public DeleteHistoricProcessInstancesCmd(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotEmpty("processInstanceIds", processInstanceIds);
    // Check if process instance is still running
    List<HistoricProcessInstance> instances = new HistoricProcessInstanceQueryImpl()
        .processInstanceIds(new HashSet<String>(this.processInstanceIds)).list();

    ensureNotEmpty("No historic process instances found ", instances);
    ensureNumberOfElements("historic process instances", instances, processInstanceIds.size());

    for (HistoricProcessInstance historicProcessInstance : instances) {

      for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkDeleteHistoricProcessInstance(historicProcessInstance);
      }

      ensureNotNull("Process instance is still running, cannot delete historic process instance: " + historicProcessInstance, "instance.getEndTime()", historicProcessInstance.getEndTime());

      String toDelete = historicProcessInstance.getId();
      commandContext
          .getHistoricProcessInstanceManager()
          .deleteHistoricProcessInstanceById(toDelete);
    }

    return null;
  }
}
