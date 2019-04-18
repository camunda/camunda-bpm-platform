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
package org.camunda.bpm.engine.spring.test.autodeployment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class DynamicResourceProducer {

  private static List<Resource> resources = new ArrayList<>();

  public static void clearResources()
  {
    resources.clear();
  }

  public static void addResource(String name, BpmnModelInstance modelInstance)
  {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(outStream, modelInstance);

    resources.add(new NamedByteArrayResource(outStream.toByteArray(), name));
  }

  public static Resource[] getResources()
  {
    return resources.toArray(new Resource[resources.size()]);
  }

  /*
   * In Spring 5, #getDescription is implemented differently
   */
  public static class NamedByteArrayResource extends ByteArrayResource
  {
    private String description;
    public NamedByteArrayResource(byte[] byteArray, String description) {
      super(byteArray, description);
      this.description = description;
    }

    @Override
    public String getDescription() {
      return description;
    }
  }
}
