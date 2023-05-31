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
package org.camunda.bpm.webapp.plugin.resource;

import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.camunda.bpm.webapp.plugin.AppPluginRegistry;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.webapp.plugin.resource.AbstractAppPluginRootResource.MIME_TYPE_TEXT_CSS;
import static org.camunda.bpm.webapp.plugin.resource.AbstractAppPluginRootResource.MIME_TYPE_TEXT_JAVASCRIPT;

@RunWith(Parameterized.class)
public class AbstractAppPluginRootResourceTest {

  public static final String PLUGIN_NAME = "test-plugin";
  public static final String ASSET_DIR = "plugin/asset-dir";
  public static final String ASSET_CONTENT = "content";

  private final String assetName;
  private final String assetMediaType;
  private final boolean assetAllowed;

  private AppRuntimeDelegate<AppPlugin> runtimeDelegate;
  private AppPluginRegistry<AppPlugin>  pluginRegistry;
  private AbstractAppPluginRootResource<AppPlugin> pluginRootResource;
  private ServletContext mockServletContext;

  public AbstractAppPluginRootResourceTest(String assetName, String assetMediaType, boolean assetAllowed) {
    this.assetName = assetName;
    this.assetMediaType = assetMediaType;
    this.assetAllowed = assetAllowed;
  }

  @Parameters
  public static Collection<Object[]> getAssets() {
    return Arrays.asList(new Object[][]{
        {"app/plugin.js", MIME_TYPE_TEXT_JAVASCRIPT, true},
        {"app/plugin.css", MIME_TYPE_TEXT_CSS, true},
        {"app/asset.js", MIME_TYPE_TEXT_JAVASCRIPT, true},
        {null, null, false},
        {"", null, false},
        {"app/plugin.cs", null, false},
        {"../..", null, false},
        {"../../annotations-api.jar", null, false},
    });
  }

  @Before
  public void setup() throws ServletException {
    runtimeDelegate = Mockito.mock(AppRuntimeDelegate.class);
    pluginRegistry = Mockito.mock(AppPluginRegistry.class);
    AppPlugin plugin = Mockito.mock(AppPlugin.class);

    Mockito.doReturn(pluginRegistry).when(runtimeDelegate).getAppPluginRegistry();
    Mockito.doReturn(plugin).when(pluginRegistry).getPlugin(PLUGIN_NAME);
    Mockito.doReturn(ASSET_DIR).when(plugin).getAssetDirectory();

    pluginRootResource = new AbstractAppPluginRootResource<>(PLUGIN_NAME, runtimeDelegate);
    mockServletContext = Mockito.mock(ServletContext.class);
    pluginRootResource.servletContext = mockServletContext;
    pluginRootResource.allowedAssets.add("app/asset.js");
    pluginRootResource.allowedAssets.add("app/asset.css");
  }

  @Test
  public void shouldGetAssetIfAllowed() throws IOException {
    // given
    String resourceName = "/" + ASSET_DIR + "/" + assetName;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(ASSET_CONTENT.getBytes());
    Mockito.doReturn(inputStream).when(mockServletContext).getResourceAsStream(resourceName);

    try {
      // when
      final Response actual = pluginRootResource.getAsset(assetName);

      // then
      if (assetAllowed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ((StreamingOutput) actual.getEntity()).write(output);

        assertThat(output.toString()).isEqualTo(ASSET_CONTENT);
        assertThat(actual.getHeaders()).containsKey(HttpHeaders.CONTENT_TYPE).hasSize(1);
        assertThat(actual.getHeaders().get(HttpHeaders.CONTENT_TYPE)).hasSize(1);
        // In IDE it's String, with maven it's MediaType class
        assertThat(actual.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0).toString()).isEqualTo(assetMediaType);

        Mockito.verify(runtimeDelegate).getAppPluginRegistry();
        Mockito.verify(pluginRegistry).getPlugin(PLUGIN_NAME);
        Mockito.verify(mockServletContext).getResourceAsStream(resourceName);
      } else {
        fail("should throw RestException for '%s'", assetName);
      }

    } catch (RestException e) {
      // then
      if (assetAllowed) {
        e.printStackTrace();
        fail("should not throw RestException for '%s'", assetName);
      } else {
        assertThat(e).isInstanceOf(RestException.class);
        assertThat(e).hasMessage("Not allowed to load the following file '" + assetName + "'.");

        Mockito.verify(runtimeDelegate, Mockito.never()).getAppPluginRegistry();
        Mockito.verify(pluginRegistry, Mockito.never()).getPlugin(PLUGIN_NAME);
        Mockito.verify(mockServletContext, Mockito.never()).getResourceAsStream(assetName);
      }
    }
  }

}
