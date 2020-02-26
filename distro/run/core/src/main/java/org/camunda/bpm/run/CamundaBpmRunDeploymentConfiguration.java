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
package org.camunda.bpm.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultDeploymentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class CamundaBpmRunDeploymentConfiguration extends DefaultDeploymentConfiguration {

  public static final String CAMUNDA_DEPLOYMENT_DIR_PROPERTY = "camunda.deploymentDir";

  @Autowired
  private Environment env;

  @Override
  public Set<Resource> getDeploymentResources() {
    String deploymentDir = env.getProperty(CAMUNDA_DEPLOYMENT_DIR_PROPERTY);
    if (!StringUtils.isEmpty(deploymentDir)) {
      Path resourceDir = Paths.get(deploymentDir);

      try (Stream<Path> stream = Files.walk(resourceDir)) {
        return stream.filter(file -> !Files.isDirectory(file)).map(FileSystemResource::new).collect(Collectors.toSet());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return Collections.emptySet();
  }
}