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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsImpl implements HistoricActivityStatistics {

  protected String id;
  protected long instances;
  protected long finished;
  protected long canceled;
  protected long completeScope;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getInstances() {
    return instances;
  }

  public void setInstances(long instances) {
    this.instances = instances;
  }

  public long getFinished() {
    return finished;
  }

  public void setFinished(long finished) {
    this.finished = finished;
  }

  public long getCanceled() {
    return canceled;
  }

  public void setCanceled(long canceled) {
    this.canceled = canceled;
  }

  public long getCompleteScope() {
    return completeScope;
  }

  public void setCompleteScope(long completeScope) {
    this.completeScope = completeScope;
  }

}
