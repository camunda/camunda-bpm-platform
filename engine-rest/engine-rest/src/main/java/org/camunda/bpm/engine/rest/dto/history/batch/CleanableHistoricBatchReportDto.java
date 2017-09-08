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

package org.camunda.bpm.engine.rest.dto.history.batch;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReport;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CleanableHistoricBatchReportDto extends AbstractQueryDto<CleanableHistoricBatchReport> {

  public CleanableHistoricBatchReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

  @Override
  protected CleanableHistoricBatchReport createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createCleanableHistoricBatchReport();
  }

  @Override
  protected void applyFilters(CleanableHistoricBatchReport query) {
  }

  @Override
  protected void applySortBy(CleanableHistoricBatchReport query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
  }

}
