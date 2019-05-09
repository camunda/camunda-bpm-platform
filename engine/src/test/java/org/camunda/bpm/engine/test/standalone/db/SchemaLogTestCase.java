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
package org.camunda.bpm.engine.test.standalone.db;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogTestCase {
  protected final static String BASE_PATH = "org/camunda/bpm/engine/db";
  protected final static String CREATE_SCRIPT_FOLDER = BASE_PATH + "/create";
  protected final static String UPGRADE_SCRIPT_FOLDER = BASE_PATH + "/upgrade";
  protected final static List<String> SCRIPT_FOLDERS = Arrays.asList(CREATE_SCRIPT_FOLDER, UPGRADE_SCRIPT_FOLDER);
  protected final static String[] DATABASES = DbSqlSessionFactory.SUPPORTED_DATABASES;

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();
  public ProcessEngine processEngine;

  protected String folderPath;
  protected Map<String, List<String>> folderContents;

  @Before
  public void init() {
    processEngine = rule.getProcessEngine();

    folderContents = new HashMap<String, List<String>>();
    for (String folder : SCRIPT_FOLDERS) {
      folderContents.put(folder, readFolderContent(folder));
    }
  }

  private List<String> readFolderContent(String path) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(path);
    assertThat(resource, CoreMatchers.notNullValue());

    File folder = new File(resource.getFile());
    assertTrue(folder.isDirectory());

    return Arrays.asList(folder.list());
  }

  public boolean isMinorLevel(String version) {
    // 7.10 -> true, 7.10.1 -> false
    return version.split("\\.").length == 2;
  }

  public boolean isPatchLevel(String version) {
    // 7.10.0 -> true, 7.10.1 -> true, 7.10 -> false
    return version.split("\\.").length == 3;
  }
}
