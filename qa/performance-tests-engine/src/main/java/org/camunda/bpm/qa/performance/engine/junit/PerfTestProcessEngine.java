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
package org.camunda.bpm.qa.performance.engine.junit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestException;

/**
 * @author Daniel Meyer
 *
 */
public class PerfTestProcessEngine {

  public static final String PROPERTIES_FILE_NAME = "perf-test-config.properties";

  protected static ProcessEngine processEngine;

  public static ProcessEngine getInstance() {
    if(processEngine == null) {

      // load properties
      Properties properties = loadProperties();
      javax.sql.DataSource datasource = createDatasource(properties);
      processEngine = createProcessEngine(datasource, properties);

    }
    return processEngine;
  }

  protected static ProcessEngine createProcessEngine(javax.sql.DataSource datasource, Properties properties) {

    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
    processEngineConfiguration.setDataSource(datasource);
    processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

    processEngineConfiguration.setHistory(properties.getProperty("historyLevel"));

    processEngineConfiguration.setJdbcBatchProcessing(Boolean.valueOf(properties.getProperty("jdbcBatchProcessing")));

    // load plugins
    String processEnginePlugins = properties.getProperty("processEnginePlugins", "");
    for (String pluginName : processEnginePlugins.split(",")) {
      if(pluginName.length() > 1) {
        Object pluginInstance = ReflectUtil.instantiate(pluginName);
        if(!(pluginInstance instanceof ProcessEnginePlugin)) {
          throw new PerfTestException("Plugin "+pluginName +" is not an instance of ProcessEnginePlugin");

        } else {
          List<ProcessEnginePlugin> plugins = processEngineConfiguration.getProcessEnginePlugins();
          if(plugins == null) {
            plugins = new ArrayList<ProcessEnginePlugin>();
            processEngineConfiguration.setProcessEnginePlugins(plugins);
          }
          plugins.add((ProcessEnginePlugin) pluginInstance);

        }
      }
    }

    return processEngineConfiguration.buildProcessEngine();
  }

  public static Properties loadProperties() {
    InputStream propertyInputStream = null;
    try {
      propertyInputStream = PerfTestProcessEngine.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
      Properties properties = new Properties();
      properties.load(propertyInputStream);
      return properties;

    } catch(Exception e) {
      throw new PerfTestException("Cannot load properties from file "+PROPERTIES_FILE_NAME+": "+e);

    } finally {
      IoUtil.closeSilently(propertyInputStream);
    }
  }

  protected static javax.sql.DataSource createDatasource(Properties properties) {

    PoolProperties p = new PoolProperties();
    p.setUrl(properties.getProperty("databaseUrl"));
    p.setDriverClassName(properties.getProperty("databaseDriver"));
    p.setUsername(properties.getProperty("databaseUser"));
    p.setPassword(properties.getProperty("databasePassword"));

    p.setJmxEnabled(false);

    p.setMaxActive(100);
    p.setInitialSize(10);

    DataSource datasource = new DataSource();
    datasource.setPoolProperties(p);

    return datasource;
  }

}
