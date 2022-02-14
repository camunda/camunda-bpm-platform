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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogUpgradeScriptPatternTest extends SchemaLogTestCase {

  @Test
  public void testOnlyValidUpgradeFilePatterns() {
    /**
     * valid patterns:
     * h2_engine_7.2_to_7.3.sql,
     * oracle_engine_7.3_patch_7.3.0_to_7.3.1.sql,
     * postgres_engine_7.3_patch_7.3.2_to_7.3.3_1.sql,
     */
    for (String file : folderContents.get(UPGRADE_SCRIPT_FOLDER)) {
      assertTrue("unexpected file format for file: " + file, file.endsWith(".sql"));
      // get rid of the .sql ending as it makes splitting easier
      file = file.substring(0, file.length() - 4);

      String[] nameParts = file.split("_");
      assertThat(nameParts[0]).isIn((Object[]) DATABASES);
      assertThat(nameParts[1]).isEqualTo("engine");
      String minorVersion = nameParts[2];
      assertTrue(isMinorLevel(minorVersion));
      if (nameParts[3].equals("to")) {
        // minor update
        assertThat(nameParts[4]).isIn(getPossibleNextVersions(minorVersion));

        assertThat(nameParts.length).isEqualTo(5);
      } else if (nameParts[3].equals("patch")) {
        // patch update
        String basePatchVersion = nameParts[4];
        assertTrue("unexpected patch version pattern for file: " + file, isPatchLevel(basePatchVersion));
        assertThat(minorVersion).isEqualTo(getMinorLevelFromPatchVersion(basePatchVersion));
        assertThat(nameParts[5]).isEqualTo("to");
        assertThat(nameParts[6]).isIn(getPossibleNextVersions(basePatchVersion));

        if (nameParts.length == 8) {
          // check that script version is integer only
          Integer.parseInt(nameParts[7]);
        } else {
          assertThat(nameParts.length).isEqualTo(7);
        }
      } else {
        fail("unexpected pattern for file: " + file);
      }
    }
  }

  private String getMinorLevelFromPatchVersion(String minorVersion) {
    String[] versionParts = minorVersion.split("\\.");
    return StringUtils.join(versionParts, ".", 0, 2);
  }

  private Object[] getPossibleNextVersions(String version) {
    List<String> versions = new ArrayList<String>();
    String[] versionParts = version.split("\\.");
    if (isPatchLevel(version)) {
      // next patch version
      versions.add(versionParts[0] + "." + versionParts[1] + "." + (Integer.parseInt(versionParts[2]) + 1));
    } else if (isMinorLevel(version)) {
      // next minor version
      versions.add(versionParts[0] + "." + (Integer.parseInt(versionParts[1]) + 1));
      // next major version
      versions.add((Integer.parseInt(versionParts[0]) + 1) + ".0");
    } else {
      fail("unexpected pattern for version: " + version);
    }
    return versions.toArray(new String[0]);
  }
}