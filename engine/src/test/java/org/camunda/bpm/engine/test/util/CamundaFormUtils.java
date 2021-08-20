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
package org.camunda.bpm.engine.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.CamundaFormDefinition;
import org.camunda.bpm.engine.test.form.deployment.FindCamundaFormDefinitionsCmd;
import org.junit.rules.TemporaryFolder;

public class CamundaFormUtils {

  public static List<CamundaFormDefinition> findAllCamundaFormDefinitionEntities(ProcessEngineConfigurationImpl config) {
    return config.getCommandExecutorTxRequired()
        .execute(new FindCamundaFormDefinitionsCmd());
  }

  public static FileInputStream writeTempFormFile(String fileName, String content, TemporaryFolder tempFolder) throws IOException {
    File formFile = new File(tempFolder.getRoot(), fileName);
    if(!formFile.exists()) {
      formFile = tempFolder.newFile(fileName);
    }

    FileWriter writer = new FileWriter(formFile, false);
    writer.write(content);
    writer.close();
    return new FileInputStream(formFile.getAbsolutePath());
  }
}
