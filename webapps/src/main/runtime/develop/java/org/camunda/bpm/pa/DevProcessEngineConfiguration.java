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
package org.camunda.bpm.pa;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;

/**
 * @author Daniel Meyer
 *
 */
public class DevProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

  protected void initDataSource() {
    PoolProperties p = new PoolProperties();
    p.setUrl("jdbc:h2:mem:activiti;MVCC=TRUE;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE");
    p.setDriverClassName("org.h2.Driver");
    p.setUsername("sa");
    p.setPassword("");

    p.setJmxEnabled(false);

    p.setMaxActive(100);
    p.setInitialSize(10);

    DataSource dataSource = new DataSource();
    dataSource.setPoolProperties(p);

    this.dataSource = dataSource;
    this.databaseType = "h2";
  }



}
