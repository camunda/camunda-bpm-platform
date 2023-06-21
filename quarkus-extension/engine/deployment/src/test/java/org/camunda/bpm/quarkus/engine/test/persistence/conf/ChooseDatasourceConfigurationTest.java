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
package org.camunda.bpm.quarkus.engine.test.persistence.conf;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.inject.Inject;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class ChooseDatasourceConfigurationTest {

  @RegisterExtension
  static QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("org/camunda/bpm/quarkus/engine/test/persistence/conf/multiple-datasources-application.properties")
      .overrideConfigKey("quarkus.camunda.datasource", "secondary")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  protected ProcessEngine processEngine;

  @Test
  public void shouldChooseDatasource() throws SQLException {
    ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
    assertThat(configuration.getDataSource().getConnection()).asString().contains("jdbc:h2:mem:secondary");
  }

}
