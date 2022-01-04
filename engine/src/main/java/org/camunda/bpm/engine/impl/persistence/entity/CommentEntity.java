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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HistoricEntity;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CommentEntity implements Comment, Event, DbEntity, HistoricEntity, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String TYPE_EVENT = "event";
  public static final String TYPE_COMMENT = "comment";

  protected String id;

  // If comments would be removeable, revision needs to be added!

  protected String type;
  protected String userId;
  protected Date time;
  protected String taskId;
  protected String processInstanceId;
  protected String action;
  protected String message;
  protected String fullMessage;
  protected String tenantId;
  protected String rootProcessInstanceId;
  protected Date removalTime;

  public Object getPersistentState() {
    return CommentEntity.class;
  }

  public byte[] getFullMessageBytes() {
    return (fullMessage != null ? StringUtil.toByteArray(fullMessage) : null);
  }

  public void setFullMessageBytes(byte[] fullMessageBytes) {
    fullMessage = (fullMessageBytes != null ? StringUtil.fromBytes(fullMessageBytes) : null );
  }

  public static String MESSAGE_PARTS_MARKER = "_|_";

  public void setMessage(String[] messageParts) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String part: messageParts) {
      if (part!=null) {
        stringBuilder.append(part.replace(MESSAGE_PARTS_MARKER, " | "));
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      } else {
        stringBuilder.append("null");
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      }
    }
    for (int i=0; i<MESSAGE_PARTS_MARKER.length(); i++) {
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
    }
    message = stringBuilder.toString();
  }

  public List<String> getMessageParts() {
    if (message==null) {
      return null;
    }
    List<String> messageParts = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(message, MESSAGE_PARTS_MARKER);
    while (tokenizer.hasMoreTokens()) {
      String nextToken = tokenizer.nextToken();
      if ("null".equals(nextToken)) {
        messageParts.add(null);
      } else {
        messageParts.add(nextToken);
      }
    }
    return messageParts;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFullMessage() {
    return fullMessage;
  }

  public void setFullMessage(String fullMessage) {
    this.fullMessage = fullMessage;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public void setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", type=" + type
           + ", userId=" + userId
           + ", time=" + time
           + ", taskId=" + taskId
           + ", processInstanceId=" + processInstanceId
           + ", rootProcessInstanceId=" + rootProcessInstanceId
           + ", removalTime=" + removalTime
           + ", action=" + action
           + ", message=" + message
           + ", fullMessage=" + fullMessage
           + ", tenantId=" + tenantId
           + "]";
  }
}
