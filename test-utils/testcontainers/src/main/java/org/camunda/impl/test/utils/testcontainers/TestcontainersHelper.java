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
package org.camunda.impl.test.utils.testcontainers;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestcontainersHelper {

  public static String resolveImageName(String imageProperty, String defaultImage) {
    String image = TestcontainersConfiguration.getInstance().getEnvVarOrProperty(imageProperty, defaultImage);
    if (image == null) {
      throw new IllegalStateException("To use the Testcontainers integration correctly, please provide a " +
          "Docker image. To do this, place a file on the classpath named `testcontainers.properties`, " +
          "containing `" + imageProperty + "=IMAGE`, where IMAGE is a suitable image name and tag.");
    } else {
      return image;
    }
  }

  public static DockerImageName resolveDockerImageName(String dbLabel, String tag, String defaultDbImage) {
    String imageProperty = dbLabel + ".container.image";
    String dockerImageString = resolveImageName(imageProperty, defaultDbImage) + ":" + tag;
    return DockerImageName.parse(dockerImageString).asCompatibleSubstituteFor(defaultDbImage);
  }

}