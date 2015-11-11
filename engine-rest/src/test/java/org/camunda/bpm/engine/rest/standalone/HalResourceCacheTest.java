/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.standalone;

import static org.camunda.bpm.engine.rest.hal.cache.HalRelationCacheConfiguration.CONFIG_CACHES;
import static org.camunda.bpm.engine.rest.hal.cache.HalRelationCacheConfiguration.CONFIG_CACHE_IMPLEMENTATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.IdentityRestService;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.cache.Cache;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.hal.HalLinkResolver;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.DefaultHalResourceCache;
import org.camunda.bpm.engine.rest.hal.cache.HalRelationCacheBootstrap;
import org.camunda.bpm.engine.rest.hal.cache.HalRelationCacheConfiguration;
import org.camunda.bpm.engine.rest.hal.cache.HalRelationCacheConfigurationException;
import org.camunda.bpm.engine.rest.hal.identitylink.HalIdentityLink;
import org.camunda.bpm.engine.rest.hal.user.HalUser;
import org.camunda.bpm.engine.task.IdentityLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class HalResourceCacheTest extends AbstractRestServiceTest {

  protected DefaultHalResourceCache cache;
  protected HalRelationCacheBootstrap contextListener;

  @Before
  public void createCache() {
    cache = new DefaultHalResourceCache(100, 100);
    contextListener = new HalRelationCacheBootstrap();
  }

  @After
  public void destroy() {
    contextListener.contextDestroyed(null);
  }

  @Test
  public void testResourceRetrieval() {
    cache.put("hello", "world");

    assertNull(cache.get(null));
    assertNull(cache.get("unknown"));
    assertEquals("world", cache.get("hello"));
  }

  @Test
  public void testCacheCapacity() {
    assertEquals(0, cache.size());

    cache.put("a", "a");
    cache.put("b", "b");
    cache.put("c", "c");
    assertEquals(3, cache.size());

    forwardTime(100);

    for (int i = 0; i < 2 * cache.getCapacity(); i++) {
      cache.put("id" + i, i);
    }
    assertTrue(cache.size() <= cache.getCapacity());

    // old entries should be removed
    assertNull(cache.get("a"));
    assertNull(cache.get("b"));
    assertNull(cache.get("c"));
  }

  @Test
  public void testEntryExpiration() {
    cache.put("hello", "world");

    assertEquals("world", cache.get("hello"));
    assertEquals(1, cache.size());

    forwardTime(cache.getSecondsToLive() + 1);

    assertNull(cache.get("hello"));
    assertEquals(0, cache.size());
  }

  @Test
  public void testInvalidConfigurationFormat() {
    try {
      contextListener.configureCaches("<!xml>");
      fail("Exception expected");
    }
    catch (HalRelationCacheConfigurationException e) {
      assertTrue(e.getCause() instanceof IOException);
    }
  }

  @Test
  public void testUnknownCacheImplementationClass() {
    try {
      contextListener.configureCaches("{\"" + CONFIG_CACHE_IMPLEMENTATION +"\": \"org.camunda.bpm.UnknownCache\" }");
      fail("Exception expected");
    }
    catch (HalRelationCacheConfigurationException e) {
      assertTrue(e.getCause() instanceof ClassNotFoundException);
    }
  }

  @Test
  public void testCacheImplementationNotImplementingCache() {
    try {
      contextListener.configureCaches("{\"" + CONFIG_CACHE_IMPLEMENTATION +"\": \"" + getClass().getName() + "\" }");
      fail("Exception expected");
    }
    catch (HalRelationCacheConfigurationException e) {
      assertTrue(e.getMessage().contains(Cache.class.getName()));
    }
  }

  @Test
  public void testCacheCreation() {
    String contextParameter = "{" +
        "\"" + CONFIG_CACHE_IMPLEMENTATION + "\": \"" + DefaultHalResourceCache.class.getName() + "\"," +
        "\"" + CONFIG_CACHES + "\": {" +
          "\"" + HalUser.class.getName() + "\": {" +
            "\"capacity\": 123, \"secondsToLive\": 123" +
          "}" +
        "}" +
      "}";

    contextListener.configureCaches(contextParameter);

    Cache cache = Hal.getInstance().getHalRelationCache(HalUser.class);
    assertNotNull(cache);
    assertEquals(123, ((DefaultHalResourceCache) cache).getCapacity());
    assertEquals(123, ((DefaultHalResourceCache) cache).getSecondsToLive());
  }

  @Test
  public void testCacheInvalidParameterName() {
    HalRelationCacheConfiguration configuration = new HalRelationCacheConfiguration();
    configuration.setCacheImplementationClass(DefaultHalResourceCache.class);
    configuration.addCacheConfiguration(HalUser.class, Collections.<String, Object>singletonMap("unknown", "property"));

    try {
      contextListener.configureCaches(configuration);
      fail("Exception expected");
    }
    catch (HalRelationCacheConfigurationException e) {
      assertTrue(e.getMessage().contains("setter"));
    }
  }

  @Test
  public void testEntityCaching() {
    String[] userIds = new String[]{"test"};
    // mock user and query
    User user = mock(User.class);
    when(user.getId()).thenReturn(userIds[0]);
    when(user.getFirstName()).thenReturn("kermit");
    UserQuery userQuery = mock(UserQuery.class);
    when(userQuery.userIdIn(Matchers.<String[]>anyVararg())).thenReturn(userQuery);
    when(userQuery.listPage(anyInt(), anyInt())).thenReturn(Arrays.asList(user));
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(userQuery);

    // configure cache
    HalRelationCacheConfiguration configuration = new HalRelationCacheConfiguration();
    configuration.setCacheImplementationClass(DefaultHalResourceCache.class);
    Map<String, Object> halUserConfig = new HashMap<String, Object>();
    halUserConfig.put("capacity", 100);
    halUserConfig.put("secondsToLive", 10000);
    configuration.addCacheConfiguration(HalUser.class, halUserConfig);

    contextListener.configureCaches(configuration);

    // cache exists and is empty
    DefaultHalResourceCache cache = (DefaultHalResourceCache) Hal.getInstance().getHalRelationCache(HalUser.class);
    assertNotNull(cache);
    assertEquals(0, cache.size());

    // get link resolver and resolve user
    HalLinkResolver linkResolver = Hal.getInstance().getLinkResolver(UserRestService.class);
    List<HalResource<?>> halUsers = linkResolver.resolveLinks(userIds, processEngine);

    // mocked user was resolved
    assertNotNull(halUsers);
    assertEquals(1, halUsers.size());
    HalUser halUser = (HalUser) halUsers.get(0);
    assertEquals("kermit", halUser.getFirstName());

    // cache contains user
    assertEquals(1, cache.size());

    // change user mock
    when(user.getFirstName()).thenReturn("fritz");

    // resolve users again
    halUsers = linkResolver.resolveLinks(userIds, processEngine);

    // cached mocked user was resolved with old name
    assertNotNull(halUsers);
    assertEquals(1, halUsers.size());
    halUser = (HalUser) halUsers.get(0);
    assertEquals("kermit", halUser.getFirstName());

    forwardTime(cache.getSecondsToLive() * 3);

    // resolve users again
    halUsers = linkResolver.resolveLinks(userIds, processEngine);

    // new mocked user was resolved with old name
    assertNotNull(halUsers);
    assertEquals(1, halUsers.size());
    halUser = (HalUser) halUsers.get(0);
    assertEquals("fritz", halUser.getFirstName());
  }

  @Test
  public void testIdentityLinkCaching() {
    String[] taskIds = new String[]{"test"};
    // mock identityLinks and query
    IdentityLink link1 = mock(IdentityLink.class);
    when(link1.getTaskId()).thenReturn(taskIds[0]);
    IdentityLink link2 = mock(IdentityLink.class);
    when(link2.getTaskId()).thenReturn(taskIds[0]);
    when(processEngine.getTaskService().getIdentityLinksForTask(anyString())).thenReturn(Arrays.asList(link1, link2));

    // configure cache
    HalRelationCacheConfiguration configuration = new HalRelationCacheConfiguration();
    configuration.setCacheImplementationClass(DefaultHalResourceCache.class);
    Map<String, Object> halIdentityLinkConfig = new HashMap<String, Object>();
    halIdentityLinkConfig.put("capacity", 100);
    halIdentityLinkConfig.put("secondsToLive", 10000);
    configuration.addCacheConfiguration(HalIdentityLink.class, halIdentityLinkConfig);

    contextListener.configureCaches(configuration);

    // cache exists and is empty
    DefaultHalResourceCache cache = (DefaultHalResourceCache) Hal.getInstance().getHalRelationCache(HalIdentityLink.class);
    assertNotNull(cache);
    assertEquals(0, cache.size());

    // get link resolver and resolve identity link
    HalLinkResolver linkResolver = Hal.getInstance().getLinkResolver(IdentityRestService.class);
    List<HalResource<?>> halIdentityLinks = linkResolver.resolveLinks(taskIds, processEngine);

    assertEquals(2, halIdentityLinks.size());
    assertEquals(1, cache.size());

    assertEquals(halIdentityLinks, cache.get(taskIds[0]));
  }

  protected void forwardTime(long seconds) {
    Date later = new Date(ClockUtil.getCurrentTime().getTime() + seconds * 1000);
    ClockUtil.setCurrentTime(later);
  }

}
