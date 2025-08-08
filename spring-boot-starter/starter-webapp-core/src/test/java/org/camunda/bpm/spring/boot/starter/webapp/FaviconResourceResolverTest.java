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
package org.camunda.bpm.spring.boot.starter.webapp;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class FaviconResourceResolverTest {

  @Mock
  Resource location;

  @InjectMocks
  FaviconResourceResolver resolver;

  @Test
  void shouldStripPathExceptLastSegment() throws IOException {
    // given
    String resourcePath = "/camunda/favicon.ico";
    String lastSegment = "/favicon.ico";
    Resource expectedFavicon = Mockito.mock(Resource.class);

    when(location.getURL()).thenReturn(new URL("file:///camunda/"));
    when(location.createRelative(lastSegment)).thenReturn(expectedFavicon);
    when(expectedFavicon.getURL()).thenReturn(new URL("file:///camunda/favicon.ico"));
    when(expectedFavicon.isReadable()).thenReturn(true);

    // when
    Resource resource = resolver.getResource(resourcePath, location);

    // then
    Assertions.assertEquals(expectedFavicon, resource);
  }

  @Test
  void shouldStripPathExceptLastSegmentWithCustomPath() throws IOException {
    // given
    String resourcePath = "/custom-path/favicon.ico";
    String lastSegment = "/favicon.ico";
    Resource expectedFavicon = Mockito.mock(Resource.class);

    when(location.getURL()).thenReturn(new URL("file:///custom-path/"));
    when(location.createRelative(lastSegment)).thenReturn(expectedFavicon);
    when(expectedFavicon.getURL()).thenReturn(new URL("file:///custom-path/favicon.ico"));
    when(expectedFavicon.isReadable()).thenReturn(true);

    // when
    Resource resource = resolver.getResource(resourcePath, location);

    // then
    Assertions.assertEquals(expectedFavicon, resource);
  }

}
