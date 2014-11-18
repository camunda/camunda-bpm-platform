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
package org.camunda.bpm.engine.rest.sub.history.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricVariableInstanceResource;
import org.camunda.bpm.engine.variable.type.ValueType;

import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Daniel Meyer
 *
 */
public class HistoricVariableInstanceResourceImpl implements HistoricVariableInstanceResource {

  protected String variableId;
  protected ProcessEngine engine;

  public HistoricVariableInstanceResourceImpl(String variableId, ProcessEngine engine) {
    this.variableId = variableId;
    this.engine = engine;
  }

  public HistoricVariableInstanceDto getVariable(boolean deserializeObjectValue) {
    HistoricVariableInstanceQuery query = baseQuery().disableBinaryFetching();

    if (!deserializeObjectValue) {
      query.disableCustomObjectDeserialization();
    }

    HistoricVariableInstance variableInstance = query.singleResult();

    if(variableInstance != null) {
      return HistoricVariableInstanceDto.fromHistoricVariableInstance(variableInstance);

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Variable instance with Id '"+variableId + "' does not exist.");

    }
  }

  public InputStream getBinaryVariable() {
    HistoricVariableInstance variableInstance = baseQuery()
        .disableCustomObjectDeserialization()
        .singleResult();
    if(variableInstance != null) {
      if (variableInstance.getTypeName().equals(ValueType.BYTES.getName())) {
        byte[] valueBytes = (byte[]) variableInstance.getValue();
        if (valueBytes == null) {
          valueBytes = new byte[0];
        }

        return new ByteArrayInputStream(valueBytes);
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            String.format("Value of variable %s is not a binary value.", variableId));
      }

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic variable instance with Id '"+variableId + "' does not exist.");
    }
  }

  protected HistoricVariableInstanceQuery baseQuery() {
    return engine.getHistoryService()
        .createHistoricVariableInstanceQuery()
        .variableId(variableId);
  }

}
