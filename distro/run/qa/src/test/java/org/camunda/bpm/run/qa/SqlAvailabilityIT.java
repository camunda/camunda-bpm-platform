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
package org.camunda.bpm.run.qa;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class SqlAvailabilityIT {

  @Test
  public void shouldFindSqlResources() throws URISyntaxException {
    Path sqlDir = Paths.get(SqlAvailabilityIT.class.getClassLoader().getResource("camunda-bpm-run-distro/configuration/sql/").toURI());

    Path createDir = sqlDir.resolve("create");
    Path dropDir = sqlDir.resolve("drop");
    Path upgradeDir = sqlDir.resolve("upgrade");

    assertThat(sqlDir).isNotNull();
    assertThat(createDir).isNotNull();
    assertThat(dropDir).isNotNull();
    assertThat(upgradeDir).isNotNull();
    assertThat(createDir.toFile().list()).hasSizeGreaterThan(0);
    assertThat(dropDir.toFile().list()).hasSizeGreaterThan(0);
    assertThat(upgradeDir.toFile().list()).hasSizeGreaterThan(0);
  }
}
