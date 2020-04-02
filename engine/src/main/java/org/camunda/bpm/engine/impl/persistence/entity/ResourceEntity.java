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

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.repository.Resource;


/**
 * @author Tom Baeyens
 */
public class ResourceEntity implements Serializable, DbEntity, Resource {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected byte[] bytes;
  protected String deploymentId;
  protected boolean generated = false;
  protected String tenantId;
  protected Integer type;
  protected Date createTime;

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

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Object getPersistentState() {
    return ResourceEntity.class;
  }

  public void setGenerated(boolean generated) {
    this.generated = generated;
  }

  /**
   * Indicated whether or not the resource has been generated while deploying rather than
   * being actual part of the deployment.
   */
  public boolean isGenerated() {
    return generated;
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

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", name=" + name
           + ", deploymentId=" + deploymentId
           + ", generated=" + generated
           + ", tenantId=" + tenantId
           + ", type=" + type
           + ", createTime=" + createTime
           + "]";
  }

}
