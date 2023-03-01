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
package org.camunda.bpm.webapp.impl.security.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Parameterized.class)
public class RequestFilterTest {

  protected static final String EMPTY_PATH = "";
  protected static final String CUSTOM_APP_PATH = "/my-custom/application/path";

  private RequestFilter matcher;

  private Map<String, String> matchResult;

  protected String applicationPath;

  @Parameterized.Parameters
  public static Collection<String> data() {
    return Arrays.asList(EMPTY_PATH, CUSTOM_APP_PATH);
  }

  public RequestFilterTest(String applicationPath) {
    this.applicationPath = applicationPath;
  }

  @Test
  public void shouldMatchMethod() {

    // given
    matcher = newMatcher("/foo/bar", "POST", "PUT");

    // when
    matchResult = matcher.match("GET", applicationPath + "/foo/bar");

    // then
    assertThat(matchResult).isNull();
  }

  @Test
  public void shouldNotMatchUri() {

    // given
    matcher = newMatcher("/foo/bar", "GET");

    // when
    matchResult = matcher.match("GET", applicationPath + "/not-matching/");

    // then
    assertThat(matchResult).isNull();
  }

  @Test
  public void shouldMatch() {

    // given
    matcher = newMatcher("/foo/bar", "GET");

    // when
    matchResult = matcher.match("GET", applicationPath + "/foo/bar");

    // then
    assertThat(matchResult).isNotNull();
  }

  @Test
  public void shouldExtractNamedUriParts() {

    // given
    matcher = newMatcher("/{foo}/{bar}", "GET");

    // when
    matchResult = matcher.match("GET", applicationPath + "/foo/bar");

    // then
    assertThat(matchResult)
        .isNotNull()
        .containsEntry("foo", "foo")
        .containsEntry("bar", "bar");
  }

  @Test
  public void shouldExtractNamedMatchAllUriPart() {

    // given
    matcher = newMatcher("/{foo}/{bar:.*}", "GET");

    // when
    matchResult = matcher.match("GET", applicationPath + "/foo/bar/asdf/asd");

    // then
    assertThat(matchResult)
        .isNotNull()
        .containsEntry("bar", "bar/asdf/asd");
  }

  private RequestFilter newMatcher(String uri, String ... methods) {
    return new RequestFilter(uri, applicationPath, methods);
  }
}
