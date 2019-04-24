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
import java.util.Date;

import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.repository.ResourceType;
import org.camunda.bpm.engine.impl.db.DbEntity;

/**
 * @author Tom Baeyens
 */
public class ByteArrayEntity implements Serializable, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  private static final Object PERSISTENTSTATE_NULL = new Object();

  protected String id;
  protected int revision;
  protected String name;
  protected byte[] bytes;
  protected String deploymentId;
  protected String tenantId;
  protected Integer type;
  protected Date createTime;
  protected String rootProcessInstanceId;
  protected Date removalTime;

  public ByteArrayEntity() {
  }

  public ByteArrayEntity(String name, byte[] bytes, ResourceType type, String rootProcessInstanceId, Date removalTime) {
    this(name, bytes, type);
    this.rootProcessInstanceId = rootProcessInstanceId;
    this.removalTime = removalTime;
  }

  public ByteArrayEntity(String name, byte[] bytes, ResourceType type) {
    this(name, bytes);
    this.type = type.getValue();
  }

  public ByteArrayEntity(String name, byte[] bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  public ByteArrayEntity(byte[] bytes, ResourceType type) {
    this.bytes = bytes;
    this.type = type.getValue();
  }

  public byte[] getBytes() {
    return bytes;
  }

  public Object getPersistentState() {
    return (bytes != null ? bytes : PERSISTENTSTATE_NULL);
  }

  public int getRevisionNext() {
    return revision+1;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
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
           + ", revision=" + revision
           + ", name=" + name
           + ", deploymentId=" + deploymentId
           + ", tenantId=" + tenantId
           + ", type=" + type
           + ", createTime=" + createTime
           + ", rootProcessInstanceId=" + rootProcessInstanceId
           + ", removalTime=" + removalTime
           + "]";
  }

}
