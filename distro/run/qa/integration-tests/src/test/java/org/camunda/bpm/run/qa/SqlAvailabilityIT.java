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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.Test;

public class SqlAvailabilityIT {

  @Test
  public void shouldFindSqlResources() throws URISyntaxException {
    Path sqlDir = Paths.get(SpringBootManagedContainer.getRunHome(), "configuration", "sql");

    Path createDir = sqlDir.resolve("create");
    Path dropDir = sqlDir.resolve("drop");
    Path upgradeDir = sqlDir.resolve("upgrade");

    assertThat(sqlDir, is(notNullValue()));
    assertThat(createDir, is(notNullValue()));
    assertThat(dropDir, is(notNullValue()));
    assertThat(upgradeDir, is(notNullValue()));
    assertThat(createDir.toFile().list().length, is(greaterThan(0)));
    assertThat(dropDir.toFile().list().length, is(greaterThan(0)));
    assertThat(upgradeDir.toFile().list().length, is(greaterThan(0)));
  }
}
