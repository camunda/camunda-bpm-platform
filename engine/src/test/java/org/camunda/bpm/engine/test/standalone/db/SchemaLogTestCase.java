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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

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
    List<String> files = new ArrayList<String>();
    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:" + path + "/*");
      assertThat(resources.length).isGreaterThan(0);
      for (Resource res : resources) {
        files.add(res.getFilename());
      }
    } catch (IOException e) {
      fail("unable to load resources from " + path);
    }

    return files;
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
