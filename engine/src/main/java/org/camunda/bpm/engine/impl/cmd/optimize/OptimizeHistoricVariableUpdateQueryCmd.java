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
package org.camunda.bpm.engine.impl.cmd.optimize;

import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;

import java.util.Date;
import java.util.List;

public class OptimizeHistoricVariableUpdateQueryCmd implements Command<List<HistoricVariableUpdate>> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected Date occurredAfter;
  protected Date occurredAt;
  protected int maxResults;

  public OptimizeHistoricVariableUpdateQueryCmd(Date occurredAfter, Date occurredAt, int maxResults) {
    this.occurredAfter = occurredAfter;
    this.occurredAt = occurredAt;
    this.maxResults = maxResults;
  }

  public List<HistoricVariableUpdate> execute(CommandContext commandContext) {
    List<HistoricVariableUpdate> historicVariableUpdates =
      commandContext.getOptimizeManager().getHistoricVariableUpdates(occurredAfter, occurredAt, maxResults);
    fetchVariableValues(historicVariableUpdates);
    return historicVariableUpdates;
  }

  private void fetchVariableValues(List<HistoricVariableUpdate> historicVariableUpdates) {
    if (historicVariableUpdates!=null) {
      for (HistoricVariableUpdate historicDetail: historicVariableUpdates) {
        if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
          HistoricDetailVariableInstanceUpdateEntity entity =
            (HistoricDetailVariableInstanceUpdateEntity) historicDetail;
          if (isNotByteArrayVariableType(entity)) {
            try {
              entity.getTypedValue(false);
            } catch(Exception t) {
              // do not fail if one of the variables fails to load
              LOG.exceptionWhileGettingValueForVariable(t);
            }
          }

        }
      }
    }
  }

  protected boolean isNotByteArrayVariableType(HistoricDetailVariableInstanceUpdateEntity entity) {
    // do not fetch values for byte arrays/ blob variables (e.g. files or bytes)
    return !AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(entity.getSerializer().getType().getName());
  }

}
