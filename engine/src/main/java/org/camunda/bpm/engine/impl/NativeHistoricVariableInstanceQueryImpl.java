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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.NativeHistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

import java.util.List;
import java.util.Map;


public class NativeHistoricVariableInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance>
        implements NativeHistoricVariableInstanceQuery {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  private static final long serialVersionUID = 1L;

  protected boolean isCustomObjectDeserializationEnabled = true;

  public NativeHistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


  //results ////////////////////////////////////////////////////////////////

  @Override
  public NativeHistoricVariableInstanceQuery disableCustomObjectDeserialization() {
        this.isCustomObjectDeserializationEnabled = false;
        return this;
  }

  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    List<HistoricVariableInstance> historicVariableInstances = commandContext
            .getHistoricVariableInstanceManager()
            .findHistoricVariableInstancesByNativeQuery(parameterMap, firstResult, maxResults);

    if (historicVariableInstances!=null) {
      for (HistoricVariableInstance historicVariableInstance: historicVariableInstances) {

        HistoricVariableInstanceEntity variableInstanceEntity = (HistoricVariableInstanceEntity) historicVariableInstance;
          try {
            variableInstanceEntity.getTypedValue(isCustomObjectDeserializationEnabled);
          } catch(Exception t) {
            // do not fail if one of the variables fails to load
            LOG.exceptionWhileGettingValueForVariable(t);
          }
      }
    }
    return historicVariableInstances;
  }

  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getHistoricVariableInstanceManager()
      .findHistoricVariableInstanceCountByNativeQuery(parameterMap);
  }

}
