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
package org.camunda.impl.test.utils.testcontainers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MSSQLServerContainerProvider;
import org.testcontainers.utility.DockerImageName;

public class CamundaMSSQLContainerProvider extends MSSQLServerContainerProvider {

  private static final String NAME = "camsqlserver";

  @Override
  public boolean supports(String databaseType) {
    return NAME.equals(databaseType);
  }

  @Override
  public JdbcDatabaseContainer newInstance(String tag) {
    DockerImageName dockerImageName = TestcontainersHelper
      .resolveDockerImageName("mssql", tag, "mcr.microsoft.com/mssql/server");
    return new MSSQLServerContainer(dockerImageName);
  }
}