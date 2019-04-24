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
package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.authorization.MissingAuthorization;

/**
 * * <p>Dto for {@link MissingAuthorization}</p>
 * @author Filip Hrisafov
 * 
 */
public class MissingAuthorizationDto {

  private String permissionName;
  private String resourceName;
  protected String resourceId;

  // transformer /////////////////////////////

  public static MissingAuthorizationDto fromInfo(MissingAuthorization info) {
    MissingAuthorizationDto dto = new MissingAuthorizationDto();

    dto.setPermissionName(info.getViolatedPermissionName());
    dto.setResourceId(info.getResourceId());
    dto.setResourceName(info.getResourceType());

    return dto;
  }

  public static List<MissingAuthorizationDto> fromInfo(Collection<MissingAuthorization> infos) {
    List<MissingAuthorizationDto> dtos = new ArrayList<MissingAuthorizationDto>();
    for (MissingAuthorization info : infos) {
      dtos.add(fromInfo(info));
    }
    return dtos;
  }

  // getter / setters ////////////////////////
  public String getPermissionName() {
    return permissionName;
  }

  public void setPermissionName(String permissionName) {
    this.permissionName = permissionName;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

}
