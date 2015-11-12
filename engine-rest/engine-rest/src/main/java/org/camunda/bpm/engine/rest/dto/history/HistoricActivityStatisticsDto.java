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
package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsDto {

  protected String id;
  protected long instances;
  protected long canceled;
  protected long finished;
  protected long completeScope;

  public HistoricActivityStatisticsDto () {}

  public String getId() {
    return id;
  }

  public long getInstances() {
    return instances;
  }

  public long getCanceled() {
    return canceled;
  }

  public long getFinished() {
    return finished;
  }

  public long getCompleteScope() {
    return completeScope;
  }

  public static HistoricActivityStatisticsDto fromHistoricActivityStatistics(HistoricActivityStatistics statistics) {
    HistoricActivityStatisticsDto result = new HistoricActivityStatisticsDto();

    result.id = statistics.getId();

    result.instances = statistics.getInstances();
    result.canceled = statistics.getCanceled();
    result.finished = statistics.getFinished();
    result.completeScope = statistics.getCompleteScope();

    return result;
  }

}
