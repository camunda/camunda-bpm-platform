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
package org.camunda.bpm.webapp.impl.engine;

import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class WebappsDatabaseNamingConsistencyTest {

  public static String COLUMN_NAME_REGEX = "([a-zA-Z_]*(?=[a-z]+)[a-zA-Z_]+_)[,\\s]";
  public static String[] SCANNED_FOLDERS = { "org/camunda/bpm/cockpit/plugin/base/queries",
      "org/camunda/bpm/admin/plugin/base/queries" };

  @Test
  public void shouldNotFindLowercaseDbColumnNamesInMappings() {
    // given the rule that all DB column names are created in uppercase

    // when scanning all mapping files for lowercase column names
    StringBuilder errorMessageBuilder = new StringBuilder();
    Pattern pattern = Pattern.compile(COLUMN_NAME_REGEX);
    try {
      for (String scannedFolder : SCANNED_FOLDERS) {
        URL resource = getClass().getClassLoader().getResource(scannedFolder);
        if (resource == null) {
          throw new IOException("Could not find path: " + scannedFolder);
        }
        File folder = new File(resource.getFile());
        File[] filesInFolder = folder.listFiles();
        for (File file : filesInFolder) {
          AtomicInteger lineNumber = new AtomicInteger(0);
          try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
              lineNumber.getAndIncrement();
              Matcher matcher = pattern.matcher(line);
              while (matcher.find()) {
                errorMessageBuilder.append(
                    "Found illegal lowercase column name " + matcher.group(1) + " in SQL " + file + " at line "
                        + lineNumber + ". All SQL column names should be uppercase.\n");
              }
            });
          }
        }
      }
    } catch (IOException e) {
      fail("Unable to find test resource for test " + getClass().getName() + "\n" + e.getMessage());
    }
    // then don't expect any results
    String errorMessage = errorMessageBuilder.toString();
    if (!errorMessage.isEmpty()) {
      fail(errorMessage);
    }
  }
}
