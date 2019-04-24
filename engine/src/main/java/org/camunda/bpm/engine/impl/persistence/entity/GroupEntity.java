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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;


/**
 * @author Tom Baeyens
 */
public class GroupEntity implements Group, Serializable, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;
  protected String name;
  protected String type;
  
  public GroupEntity() {
  }
  
  public GroupEntity(String id) {
    this.id = id;
  }
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("type", type);
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

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
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", revision=" + revision
           + ", name=" + name
           + ", type=" + type
           + "]";
  }
}
