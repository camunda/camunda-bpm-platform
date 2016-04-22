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
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricBatchQueryDto extends AbstractQueryDto<HistoricBatchQuery> {

  private static final String SORT_BY_BATCH_ID_VALUE = "batchId";
  private static final String SORT_BY_BATCH_START_TIME_VALUE = "startTime";
  private static final String SORT_BY_BATCH_END_TIME_VALUE = "endTime";

  protected String batchId;
  protected String type;
  protected Boolean completed;

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_BATCH_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_BATCH_START_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_BATCH_END_TIME_VALUE);
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

  @CamundaQueryParam(value = "completed", converter = BooleanConverter.class)
  public void setCompleted(Boolean completed) {
    this.completed = completed;
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

    if (completed != null) {
      query.completed(completed);
    }
  }

  protected void applySortBy(HistoricBatchQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_BATCH_ID_VALUE)) {
      query.orderById();
    }
    if (sortBy.equals(SORT_BY_BATCH_START_TIME_VALUE)) {
      query.orderByStartTime();
    }
    if (sortBy.equals(SORT_BY_BATCH_END_TIME_VALUE)) {
      query.orderByEndTime();
    }
  }

}
