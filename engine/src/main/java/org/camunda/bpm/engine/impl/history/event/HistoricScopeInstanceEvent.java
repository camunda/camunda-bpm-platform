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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;

/**
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
public class HistoricScopeInstanceEvent extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  protected Long durationInMillis;
  protected Date startTime;
  protected Date endTime;

  // getters / setters ////////////////////////////////////

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Long getDurationInMillis() {
    if(durationInMillis != null) {
      return durationInMillis;

    } else if(startTime != null && endTime != null) {
      return endTime.getTime() - startTime.getTime();

    } else {
      return null;

    }
  }

  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
  }

  public Long getDurationRaw() {
    return durationInMillis;
  }

}
