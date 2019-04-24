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

import java.util.*;

import org.camunda.bpm.engine.repository.*;
import org.camunda.bpm.engine.rest.dto.LinkableDto;

public class DeploymentDto extends LinkableDto {

  protected String id;
  protected String name;
  protected String source;
  protected Date deploymentTime;
  protected String tenantId;

  public DeploymentDto() {
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

  public Date getDeploymentTime() {
    return deploymentTime;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static DeploymentDto fromDeployment(Deployment deployment) {
    DeploymentDto dto = new DeploymentDto();
    dto.id = deployment.getId();
    dto.name = deployment.getName();
    dto.source = deployment.getSource();
    dto.deploymentTime = deployment.getDeploymentTime();
    dto.tenantId = deployment.getTenantId();
    return dto;
  }

}
