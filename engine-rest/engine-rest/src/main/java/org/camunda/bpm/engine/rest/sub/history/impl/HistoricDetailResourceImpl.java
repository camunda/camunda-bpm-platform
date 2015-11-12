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

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.AbstractResourceProvider;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 * @author Ronny Bräunlich
 *
 */
public class HistoricDetailResourceImpl extends AbstractResourceProvider<HistoricDetailQuery, HistoricDetail, HistoricDetailDto> implements
    HistoricDetailResource {

  public HistoricDetailResourceImpl(String detailId, ProcessEngine engine) {
    super(detailId, engine);
  }

  protected HistoricDetailQuery baseQuery() {
    return engine.getHistoryService().createHistoricDetailQuery().detailId(getId());
  }

  @Override
  protected Query<HistoricDetailQuery, HistoricDetail> baseQueryForBinaryVariable() {
    return baseQuery().disableCustomObjectDeserialization();
  }

  @Override
  protected Query<HistoricDetailQuery, HistoricDetail> baseQueryForVariable(boolean deserializeObjectValue) {
    HistoricDetailQuery query = baseQuery().disableBinaryFetching();

    if (!deserializeObjectValue) {
      query.disableCustomObjectDeserialization();
    }
    return query;
  }

  @Override
  protected TypedValue transformQueryResultIntoTypedValue(HistoricDetail queryResult) {
    if (!(queryResult instanceof HistoricVariableUpdate)) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Historic detail with Id '" + getId() + "' is not a variable update.");
    }
    HistoricVariableUpdate update = (HistoricVariableUpdate) queryResult;
    return update.getTypedValue();
  }

  @Override
  protected HistoricDetailDto transformToDto(HistoricDetail queryResult) {
    return HistoricDetailDto.fromHistoricDetail(queryResult);
  }

  @Override
  protected String getResourceNameForErrorMessage() {
    return "Historic detail";
  }

}
