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
package org.camunda.bpm.engine.impl.history;

import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Tassilo Weidner
 */
public class DefaultHistoryRemovalTimeProvider implements HistoryRemovalTimeProvider {

  public Date calculateRemovalTime(HistoricProcessInstanceEventEntity historicRootProcessInstance, ProcessDefinition processDefinition) {

    Integer historyTimeToLive = processDefinition.getHistoryTimeToLive();

    if (historyTimeToLive != null) {
      if (isProcessInstanceRunning(historicRootProcessInstance)) {
        Date startTime = historicRootProcessInstance.getStartTime();
        return determineRemovalTime(startTime, historyTimeToLive);

      } else if (isProcessInstanceEnded(historicRootProcessInstance)) {
        Date endTime = historicRootProcessInstance.getEndTime();
        return determineRemovalTime(endTime, historyTimeToLive);

      }
    }

    return null;
  }

  protected boolean isProcessInstanceRunning(HistoricProcessInstanceEventEntity historicProcessInstance) {
    return historicProcessInstance.getEndTime() == null;
  }

  protected boolean isProcessInstanceEnded(HistoricProcessInstanceEventEntity historicProcessInstance) {
    return historicProcessInstance.getEndTime() != null;
  }

  protected Date determineRemovalTime(Date initTime, Integer timeToLive) {
    Calendar removalTime = Calendar.getInstance();
    removalTime.setTime(initTime);
    removalTime.add(Calendar.DATE, timeToLive);

    return removalTime.getTime();
  }

}
