/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.test.deployment.war;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * @author Falko Menge
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithDiagram extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchive.bpmn20.xml")
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchive.png");
  }
  
  @Test
  public void testDeployProcessArchive() throws IOException {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchive")
      .singleResult();
    assertNotNull(processDefinition);
    InputStream actualStream = repositoryService.getProcessDiagram(processDefinition.getId());
    assertNotNull(actualStream);
    assertTrue(0 < actualStream.available());
    InputStream expectedStream = getClass().getResourceAsStream("/com/camunda/fox/platform/test/testDeployProcessArchive.png");
    assertNotNull(expectedStream);
    assertTrue(isEqual(expectedStream, actualStream));
  }

  protected static boolean isEqual(InputStream stream1, InputStream stream2)
          throws IOException {

      ReadableByteChannel channel1 = Channels.newChannel(stream1);
      ReadableByteChannel channel2 = Channels.newChannel(stream2);

      ByteBuffer buffer1 = ByteBuffer.allocateDirect(1024);
      ByteBuffer buffer2 = ByteBuffer.allocateDirect(1024);

      try {
          while (true) {

              int bytesReadFromStream1 = channel1.read(buffer1);
              int bytesReadFromStream2 = channel2.read(buffer2);

              if (bytesReadFromStream1 == -1 || bytesReadFromStream2 == -1) return bytesReadFromStream1 == bytesReadFromStream2;

              buffer1.flip();
              buffer2.flip();

              for (int i = 0; i < Math.min(bytesReadFromStream1, bytesReadFromStream2); i++)
                  if (buffer1.get() != buffer2.get())
                      return false;

              buffer1.compact();
              buffer2.compact();
          }

      } finally {
          if (stream1 != null) stream1.close();
          if (stream2 != null) stream2.close();
      }
  }
  
}
