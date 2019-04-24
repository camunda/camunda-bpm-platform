/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;

/**
 * @author Daniel Meyer
 *
 */
public class HistoricFormPropertyEventEntity extends HistoricDetailEventEntity {

  private static final long serialVersionUID = 1L;

  protected String propertyId;
  protected String propertyValue;

  public HistoricFormPropertyEventEntity() {
  }

  public String getPropertyId() {
    return propertyId;
  }

  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  public Object getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }

  public Date getTime() {
    return timestamp;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[propertyId=" + propertyId
           + ", propertyValue=" + propertyValue
           + ", activityInstanceId=" + activityInstanceId
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", id=" + id
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", taskId=" + taskId
           + ", tenantId=" + tenantId
           + "]";
  }

}
