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
package org.camunda.bpm.engine.impl.repository;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.CalledProcessDefinition;

public class CalledProcessDefinitionImpl implements CalledProcessDefinition {
  protected String name;
  protected int version;
  protected String key;
  protected List<String> callActivityIds;
  protected String id;
  protected String callingProcessDefinitionId;

  public CalledProcessDefinitionImpl(ProcessDefinition calledProcessDefinition, String callingProcessDefinitionId) {
    this.callActivityIds = new ArrayList<>();
    this.id = calledProcessDefinition.getId();
    this.key = calledProcessDefinition.getKey();
    this.version = calledProcessDefinition.getVersion();
    this.name = calledProcessDefinition.getName();
    this.callingProcessDefinitionId = callingProcessDefinitionId;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public int getVersion() {
    return this.version;
  }

  @Override
  public String getCallingProcessDefinitionId() {
    return null;
  }

  @Override
  public List<String> getCallingCallActivityIds() {
    return this.callActivityIds;
  }

  public void addCallingCallActivity(String activityId) {
    this.callActivityIds.add(activityId);
  }

}
