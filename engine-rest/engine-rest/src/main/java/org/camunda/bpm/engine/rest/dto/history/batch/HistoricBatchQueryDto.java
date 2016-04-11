/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.dto.history.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricBatchQueryDto extends AbstractQueryDto<HistoricBatchQuery> {

  private static final String SORT_BY_BATCH_ID_VALUE = "batchId";

  protected String batchId;
  protected String type;

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_BATCH_ID_VALUE);
  }

  public HistoricBatchQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("batchId")
  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  @CamundaQueryParam("type")
  public void setType(String type) {
    this.type = type;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected HistoricBatchQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricBatchQuery();
  }

  protected void applyFilters(HistoricBatchQuery query) {
    if (batchId != null) {
      query.batchId(batchId);
    }

    if (type != null) {
      query.type(type);
    }
  }

  protected void applySortBy(HistoricBatchQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_BATCH_ID_VALUE)) {
      query.orderById();
    }
  }

}
