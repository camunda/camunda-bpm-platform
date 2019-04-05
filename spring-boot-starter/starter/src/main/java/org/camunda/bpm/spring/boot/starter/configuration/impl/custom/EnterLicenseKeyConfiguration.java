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
package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;


import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.CamundaBpmNestedRuntimeException;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class EnterLicenseKeyConfiguration extends AbstractCamundaConfiguration {

  private static final String TABLE_PREFIX_PLACEHOLDER = "{TABLE_PREFIX}";



  private static final String INSERT_SQL = "INSERT INTO " + TABLE_PREFIX_PLACEHOLDER + "ACT_GE_PROPERTY VALUES ('camunda-license-key', ?, 1)";
  private static final String SELECT_SQL = "SELECT VALUE_ FROM " + TABLE_PREFIX_PLACEHOLDER + "ACT_GE_PROPERTY where NAME_ = 'camunda-license-key'";

  @Autowired
  private CamundaBpmVersion version;

  private String defaultLicenseFile = "camunda-license.txt";

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    if (!version.isEnterprise()) {
      return;
    }

    URL fileUrl = camundaBpmProperties.getLicenseFile();


    Optional<String> licenseKey = readLicenseKeyFromUrl(fileUrl);
    if (!licenseKey.isPresent()) {
      fileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource(defaultLicenseFile);
      licenseKey = readLicenseKeyFromUrl(fileUrl);
    }

    if (!licenseKey.isPresent()) {
      return;
    }

    try (Connection connection = dataSource(processEngine).getConnection()) {
      if (readLicenseKeyFromDatasource(connection).isPresent()) {
        return;
      }
      try (PreparedStatement statement = connection.prepareStatement(getSql(INSERT_SQL))) {
        statement.setString(1, licenseKey.get());
        statement.execute();
      }
      connection.commit();
      LOG.enterLicenseKey(fileUrl);
    } catch (SQLException ex) {
      throw new CamundaBpmNestedRuntimeException(ex.getMessage(), ex);
    }
  }

  private  DataSource dataSource(ProcessEngine processEngine) {
    return processEngine.getProcessEngineConfiguration().getDataSource();
  }

  protected Optional<String> readLicenseKeyFromDatasource(Connection connection) throws SQLException {
    final ResultSet resultSet = connection.createStatement().executeQuery(getSql(SELECT_SQL));
    return resultSet.next() ? Optional.ofNullable(resultSet.getString(1)) : Optional.empty();
  }

  protected Optional<String> readLicenseKeyFromUrl(URL licenseFileUrl) {
    if (licenseFileUrl == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(new Scanner(licenseFileUrl.openStream(), "UTF-8").useDelimiter("\\A"))
        .filter(Scanner::hasNext).map(Scanner::next)
        .map(s -> s.split("---------------")[2])
        .map(s -> s.replaceAll("\\n", ""))
        .map(String::trim);
    } catch (IOException e) {
      LOG.enterLicenseKeyFailed(licenseFileUrl, e);
      return Optional.empty();
    }
  }

  private String getSql(String sqlTemplate) {
    String tablePrefix = camundaBpmProperties.getDatabase().getTablePrefix();
    if (tablePrefix == null) {
      tablePrefix = "";
    }
    return sqlTemplate.replace(TABLE_PREFIX_PLACEHOLDER, tablePrefix);
  }

}
