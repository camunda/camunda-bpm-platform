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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This test should not be run on our CI, as it requires a Docker-in-Docker image to run successfully.
 */
@Ignore
@RunWith(Parameterized.class)
public class DatabaseContainerProviderTest {


  @Parameterized.Parameter(0)
  public String jdbcUrl;
  @Parameterized.Parameter(1)
  public String versionStatement;
  @Parameterized.Parameter(2)
  public String dbVersion;

  @Parameterized.Parameters(name = "Job DueDate is set: {0}")
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
      // The Camunda PostgreSQL 13.2 image is compatible with Testcontainers.
      // For older versions, please use the public Docker images (DockerHub repo: postgres).
      { "jdbc:tc:campostgresql:13.2:///process-engine", "SELECT version();", "13.2" },
      // The current Camunda MariaDB images are compatible with Testcontainers.
      // The username and password need to be explicitly declared.
      { "jdbc:tc:cammariadb:10.0://localhost:3306/process-engine?user=camunda&password=camunda", "SELECT version();", "10.0" },
      // The current Camunda MySQL images are compatible with Testcontainers.
      // The username and password need to be explicitly declared.
      { "jdbc:tc:cammysql:5.7://localhost:3306/process-engine?user=camunda&password=camunda", "SELECT version();", "5.7" },
      { "jdbc:tc:cammysql:8.0://localhost:3306/process-engine?user=camunda&password=camunda", "SELECT version();", "8.0" },
      // The current Camunda SqlServer 2017/2019 images are compatible with Testcontainers.
      { "jdbc:tc:camsqlserver:2017:///process-engine", "SELECT @@VERSION", "2017" },
      { "jdbc:tc:camsqlserver:2019:///process-engine", "SELECT @@VERSION", "2019" },
      // The current Camunda DB2 images are not compatible with Testcontainers.
//      { "jdbc:tc:camdb2:11.1:///engine?user=camunda&password=camunda", "SELECT * FROM SYSIBMADM.ENV_INST_INFO;", "11.1"},
      // The current Camunda Oracle images are not compatible with Testcontainers.
//      { "jdbc:tc:camoracle:thin:@localhost:1521:xe?user=camunda&password=camunda", "SELECT * FROM v$version;", "18" }
    });
  }

  @Test
  public void testJdbcTestcontainersUrl() {
    // when
    try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
      connection.setAutoCommit(false);
      ResultSet rs = connection.prepareStatement(versionStatement).executeQuery();
      if (rs.next()) {
        // then
        String version = rs.getString(1);
        assertThat(version).contains(dbVersion);
      }
    } catch (SQLException throwables) {
      fail("Testcontainers failed to spin up a Docker container: " + throwables.getMessage());
    }
  }

}