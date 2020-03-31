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
package org.camunda.bpm.engine.impl.batch;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Aggregated information on deployment ids and the number of related resources
 */
public class DeploymentMapping {
  protected static String NULL_ID = "$NULL";

  protected String deploymentId;
  protected int count;

  public DeploymentMapping(String deploymentId, int count) {
    this.deploymentId = deploymentId == null ? NULL_ID : deploymentId;
    this.count = count;
  }

  public String getDeploymentId() {
    return NULL_ID.equals(deploymentId) ? null : deploymentId;
  }

  public int getCount() {
    return count;
  }

  public List<String> getIds(List<String> ids) {
    return ids.subList(0, count);
  }

  public void removeIds(int numberOfIds) {
    count -= numberOfIds;
  }

  @Override
  public String toString() {
    return new StringJoiner(";").add(deploymentId).add(String.valueOf(count)).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, deploymentId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DeploymentMapping)) {
      return false;
    }
    DeploymentMapping other = (DeploymentMapping) obj;
    return count == other.count && Objects.equals(deploymentId, other.deploymentId);
  }

}