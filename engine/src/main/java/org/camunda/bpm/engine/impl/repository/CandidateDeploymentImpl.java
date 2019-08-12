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

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.repository.CandidateDeployment;
import org.camunda.bpm.engine.repository.Resource;

import java.util.HashMap;
import java.util.Map;

public class CandidateDeploymentImpl implements CandidateDeployment {

  protected String name;
  protected Map<String, Resource> resources;

  public CandidateDeploymentImpl(String name, Map<String, Resource> resources) {
    this.name = name;
    this.resources = resources;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Map<String, Resource> getResources() {
    return resources;
  }

  public void setResources(Map<String, Resource> resources) {
    this.resources = resources;
  }

  public static CandidateDeploymentImpl fromDeploymentEntity(DeploymentEntity deploymentEntity) {
    // first cast ResourceEntity map to Resource
    Map<String, Resource> resources = new HashMap<>((Map<String, ? extends Resource>) deploymentEntity.getResources());
    return new CandidateDeploymentImpl(deploymentEntity.getName(), resources);
  }
}
