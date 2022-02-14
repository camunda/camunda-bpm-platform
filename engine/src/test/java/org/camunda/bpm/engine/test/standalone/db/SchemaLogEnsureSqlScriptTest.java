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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.test.util.TestconfigProperties;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogEnsureSqlScriptTest extends SchemaLogTestCase {

  protected String currentSchemaVersion;
  protected String dataBaseType;

  @Override
  @Before
  public void init() {
    super.init();

    SchemaLogEntry latestEntry = processEngine.getManagementService().createSchemaLogQuery().orderByTimestamp().desc().listPage(0, 1).get(0);
    currentSchemaVersion = latestEntry.getVersion();

    dataBaseType = processEngine.getProcessEngineConfiguration().getDatabaseType();
  }

  @Test
  public void ensureUpgradeScriptsUpdateSchemaLogVersion() {
    List<String> scriptsForDB = new ArrayList<>();
    for (String file : folderContents.get(UPGRADE_SCRIPT_FOLDER)) {
      if (file.startsWith(dataBaseType)) {
        scriptsForDB.add(file);
      }
    }

    if (!scriptsForDB.isEmpty()) {
      assertThat(getLatestTargetVersion(scriptsForDB)).isEqualTo(currentSchemaVersion);
    } else {
      // databases that are newly added have no update scripts yet
      assertThat(getCurrentMinorVersion()).isEqualTo(currentSchemaVersion);
    }
  }

  @Test
  public void ensureOnlyScriptsForValidDatabaseTypes() {
    for (String file : folderContents.get(UPGRADE_SCRIPT_FOLDER)) {
      assertThat(file.split("_")[0]).isIn((Object[]) DATABASES);
    }
  }

  protected String getTargetVersionForScript(String file) {
    String targetVersion = file.substring(file.indexOf("to_") + 3).replace(".sql", "");
    if(isMinorLevel(targetVersion)) {
      targetVersion += ".0";
    }
    return targetVersion;
  }

  protected String getLatestTargetVersion(List<String> scriptFiles) {
    String latestVersion = null;
    for (String file : scriptFiles) {
      if(latestVersion == null) {
        latestVersion = getTargetVersionForScript(file);
      } else {
        String targetVersion = getTargetVersionForScript(file);
        if(isLaterVersionThan(targetVersion, latestVersion)){
          latestVersion = targetVersion;
        }
      }
    }
    return latestVersion;
  }

  protected boolean isLaterVersionThan(String v1, String v2) {
    String[] v1_ = v1.split("\\.|_");
    String[] v2_ = v2.split("\\.|_");

    int length = Math.max(v1_.length, v2_.length);
    for (int i = 0; i < length; i++) {
      int v1Part = i < v1_.length ? Integer.parseInt(v1_[i]) : 0;
      int v2Part = i < v2_.length ? Integer.parseInt(v2_[i]) : 0;
      if(v1Part != v2Part) {
        return v1Part > v2Part;
      }
    }
    return false;
  }

  protected String getCurrentMinorVersion() {
    String version = TestconfigProperties.getEngineVersion();
    // remove the patch version, and create a "clean" minor version
    int lastPos = version.lastIndexOf(".");
    version = version.substring(0, lastPos);

    return version + ".0";
  }
}