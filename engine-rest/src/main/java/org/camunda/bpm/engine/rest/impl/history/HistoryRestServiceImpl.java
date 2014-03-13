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
package org.camunda.bpm.engine.rest.impl.history;

import org.camunda.bpm.engine.rest.history.HistoricActivityInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricActivityStatisticsRestService;
import org.camunda.bpm.engine.rest.history.HistoricDetailRestService;
import org.camunda.bpm.engine.rest.history.HistoricIncidentRestService;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.engine.rest.history.UserOperationLogRestService;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;

public class HistoryRestServiceImpl extends AbstractRestProcessEngineAware implements HistoryRestService {

  public HistoryRestServiceImpl() {
    super();
  }

  public HistoryRestServiceImpl(String engineName) {
    super(engineName);
  }

  @Override
  public HistoricProcessInstanceRestService getProcessInstanceService() {
    return new HistoricProcessInstanceRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricActivityInstanceRestService getActivityInstanceService() {
    return new HistoricActivityInstanceRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricVariableInstanceRestService getVariableInstanceService() {
    return new HistoricVariableInstanceRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricActivityStatisticsRestService getActivityStatisticsService() {
    return new HistoricActivityStatisticsRestServiceImpl(getProcessEngine());
  }

  @Override
  public UserOperationLogRestService getUserOperationLogRestService() {
    return new UserOperationLogRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricDetailRestService getDetailService() {
    return new HistoricDetailRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricTaskInstanceRestService getTaskInstanceService() {
    return new HistoricTaskInstanceRestServiceImpl(getProcessEngine());
  }

  @Override
  public HistoricIncidentRestService getIncidentService() {
    return new HistoricIncidentRestServiceImpl(getProcessEngine());
  }

}
