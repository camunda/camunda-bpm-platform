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

package org.camunda.bpm.engine.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.variable.ByteArrayType;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  private final static Logger LOGGER = Logger.getLogger(HistoricDetailQueryImpl.class.getName());

  private static final long serialVersionUID = 1L;
  protected String detailId;
  protected String taskId;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String type;
  protected String variableInstanceId;

  protected boolean excludeTaskRelated = false;
  protected boolean isByteArrayFetchingEnabled = true;

  public HistoricDetailQueryImpl() {
  }

  public HistoricDetailQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDetailQuery detailId(String id) {
    assertParamNotNull("detailId", id);
    this.detailId = id;
    return this;
  }

  public HistoricDetailQuery variableInstanceId(String variableInstanceId) {
    assertParamNotNull("variableInstanceId", variableInstanceId);
    this.variableInstanceId = variableInstanceId;
    return this;
  }

  public HistoricDetailQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricDetailQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricDetailQuery activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricDetailQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricDetailQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoricDetailQuery formProperties() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery formFields() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery variableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }

  public HistoricDetailQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDetailManager()
      .findHistoricDetailCountByQueryCriteria(this);
  }

  public HistoricDetailQuery disableBinaryFetching() {
    this.isByteArrayFetchingEnabled = false;
    return this;
  }

  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<HistoricDetail> historicDetails = commandContext
      .getHistoricDetailManager()
      .findHistoricDetailsByQueryCriteria(this, page);
    if (historicDetails!=null) {
      for (HistoricDetail historicDetail: historicDetails) {
        if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
          HistoricDetailVariableInstanceUpdateEntity entity = (HistoricDetailVariableInstanceUpdateEntity) historicDetail;
          // do not fetch values for byte arrays eagerly (unless requested by the user)
          if (isByteArrayFetchingEnabled
              || !ByteArrayType.TYPE_NAME.equals(entity.getVariableType().getTypeName())) {

            try {
              entity.getValue();
            } catch(Exception t) {
              // do not fail if one of the variables fails to load
              LOGGER.log(Level.FINE, "Exception while getting value for variable", t);
            }

          }

        }
      }
    }
    return historicDetails;
  }

  // order by /////////////////////////////////////////////////////////////////

  public HistoricDetailQuery orderByProcessInstanceId() {
    orderBy(HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricDetailQuery orderByTime() {
    orderBy(HistoricDetailQueryProperty.TIME);
    return this;
  }

  public HistoricDetailQuery orderByVariableName() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQuery orderByFormPropertyId() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQuery orderByVariableRevision() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_REVISION);
    return this;
  }

  public HistoricDetailQuery orderByVariableType() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_TYPE);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getType() {
    return type;
  }

  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }

  public String getDetailId() {
    return detailId;
  }
}
