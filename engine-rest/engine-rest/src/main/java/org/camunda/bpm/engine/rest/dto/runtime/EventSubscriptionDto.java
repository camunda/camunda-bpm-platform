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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Date;

import org.camunda.bpm.engine.runtime.EventSubscription;

public class EventSubscriptionDto {

  private String id;
  private String eventType;
  private String eventName;
  private String executionId;
  private String processInstanceId;
  private String activityId;
  private Date createdDate;
  private String tenantId;

  public String getId() {
    return id;
  }
  public String getEventType() {
    return eventType;
  }
  public String getEventName() {
    return eventName;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getActivityId() {
    return activityId;
  }
  public Date getCreatedDate() {
    return createdDate;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static EventSubscriptionDto fromEventSubscription(EventSubscription eventSubscription) {
    EventSubscriptionDto dto = new EventSubscriptionDto();
    dto.id = eventSubscription.getId();
    dto.eventType = eventSubscription.getEventType();
    dto.eventName = eventSubscription.getEventName();
    dto.executionId = eventSubscription.getExecutionId();
    dto.processInstanceId = eventSubscription.getProcessInstanceId();
    dto.activityId = eventSubscription.getActivityId();
    dto.createdDate = eventSubscription.getCreated();
    dto.tenantId = eventSubscription.getTenantId();

    return dto;
  }


}
