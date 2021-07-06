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
package org.camunda.bpm.engine.rest.dto.repository;

import java.util.List;

import org.camunda.bpm.engine.repository.StaticCalledProcessDefinition;

public class StaticCalledProcessDefinitionDto {

  private String name;
  private int version;
  private String key;
  private List<String> callActivityIds;
  private String id;
  private String callingProcessDefinitionId;

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getKey() {
    return key;
  }

  public List<String> getCallActivityIds() {
    return callActivityIds;
  }

  public String getId() {
    return id;
  }

  public String getCallingProcessDefinitionId() {
    return callingProcessDefinitionId;
  }

  public static StaticCalledProcessDefinitionDto from(StaticCalledProcessDefinition calledDefinition){
    StaticCalledProcessDefinitionDto dto = new StaticCalledProcessDefinitionDto();
    dto.callingProcessDefinitionId = calledDefinition.getCallingProcessDefinitionId();
    dto.callActivityIds = calledDefinition.getCallingCallActivityIds();
    dto.id = calledDefinition.getId();
    dto.key = calledDefinition.getKey();
    dto.name = calledDefinition.getName();
    dto.version = calledDefinition.getVersion();

    return dto;
  }
}
