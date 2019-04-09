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
package org.camunda.bpm.engine.rest.dto.identity;

import org.camunda.bpm.engine.identity.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class GroupDto {

  protected String id;
  protected String name;
  protected String type;
  
  // Transformers ////////////////////////////////
  
  public static GroupDto fromGroup(Group dbGroup) {
    GroupDto groupDto = new GroupDto();
    groupDto.setId(dbGroup.getId());
    groupDto.setName(dbGroup.getName());
    groupDto.setType(dbGroup.getType());
    return groupDto;
  }
  
  public void update(Group dbGroup) {
    dbGroup.setId(id);
    dbGroup.setName(name);
    dbGroup.setType(type);
  }
  
  public static List<GroupDto> fromGroupList(List<Group> dbGroupList) {
    List<GroupDto> resultList = new ArrayList<GroupDto>();
    for (Group group : dbGroupList) {
      resultList.add(fromGroup(group));
    }
    return resultList;
  }
  
  // Getters / Setters ///////////////////////////

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
  
}
