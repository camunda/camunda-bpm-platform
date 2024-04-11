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
package org.camunda.bpm.container.impl.jboss.extension;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.SubsystemModel;

public enum MysubsystemModel implements SubsystemModel {
  VERSION_1_1(1,1),
  ;
  static final MysubsystemModel CURRENT = VERSION_1_1;

  private final ModelVersion version;

  MysubsystemModel(int major, int minor) {
      this.version = ModelVersion.create(major, minor);
  }

  @Override
  public ModelVersion getVersion() {
      return this.version;
  }
}
