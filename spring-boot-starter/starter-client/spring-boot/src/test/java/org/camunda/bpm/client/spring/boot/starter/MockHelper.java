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
package org.camunda.bpm.client.spring.boot.starter;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.mockito.MockedStatic;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class MockHelper {

  protected static MockedStatic<ExternalTaskClient> mockedStatic;
  protected static ExternalTaskClientBuilder clientBuilder;

  public static void initMocks() {
    assumeTrue(jdkSupportsMockito());

    mockedStatic = mockStatic(ExternalTaskClient.class);
    clientBuilder = mock(ExternalTaskClientBuilder.class, RETURNS_SELF);
    when(ExternalTaskClient.create()).thenReturn(clientBuilder);
    ExternalTaskClient client = mock(ExternalTaskClient.class);
    when(clientBuilder.build()).thenReturn(client);
  }

  public static void reset() {
    if(jdkSupportsMockito()) {
      mockedStatic.close();
    }
  }

  public static ExternalTaskClientBuilder getClientBuilder() {
    return clientBuilder;
  }

  public static boolean jdkSupportsMockito() {
    String jvmVendor = System.getProperty("java.vm.vendor");
    String javaVersion = System.getProperty("java.version");

    boolean isIbmJDK = jvmVendor != null && jvmVendor.contains("IBM");
    boolean isJava8 = javaVersion != null && javaVersion.startsWith("1.8");

    return !(isIbmJDK && isJava8);
  }

}
