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
package org.camunda.bpm.engine.impl.batch.removaltime;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeBatchConfiguration extends BatchConfiguration {

  protected Date removalTime;
  protected boolean hasRemovalTime;
  protected boolean isHierarchical;

  public SetRemovalTimeBatchConfiguration(List<String> ids) {
    this(ids, null);
  }

  public SetRemovalTimeBatchConfiguration(List<String> ids, DeploymentMappings mappings) {
    super(ids, mappings);
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public SetRemovalTimeBatchConfiguration setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
    return this;
  }

  public boolean hasRemovalTime() {
    return hasRemovalTime;
  }

  public SetRemovalTimeBatchConfiguration setHasRemovalTime(boolean hasRemovalTime) {
    this.hasRemovalTime = hasRemovalTime;
    return this;
  }

  public boolean isHierarchical() {
    return isHierarchical;
  }

  public SetRemovalTimeBatchConfiguration setHierarchical(boolean hierarchical) {
    isHierarchical = hierarchical;
    return this;
  }

}
