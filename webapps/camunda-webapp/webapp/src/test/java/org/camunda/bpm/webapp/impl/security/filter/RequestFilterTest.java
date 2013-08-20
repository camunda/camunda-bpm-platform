/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.filter;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

import java.util.Map;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class RequestFilterTest {

  private RequestFilter matcher;

  private Map<String, String> matchResult;

  @Test
  public void shouldMatchMethod() {

    // given
    matcher = newMatcher("/foo/bar", "POST", "PUT");

    // when
    matchResult = matcher.match("GET", "/foo/bar");

    // then
    assertThat(matchResult).isNull();
  }

  @Test
  public void shouldNotMatchUri() {

    // given
    matcher = newMatcher("/foo/bar", "GET");

    // when
    matchResult = matcher.match("GET", "/not-matching/");

    // then
    assertThat(matchResult).isNull();
  }

  @Test
  public void shouldMatch() {

    // given
    matcher = newMatcher("/foo/bar", "GET");

    // when
    matchResult = matcher.match("GET", "/foo/bar");

    // then
    assertThat(matchResult).isNotNull();
  }

  @Test
  public void shouldExtractNamedUriParts() {

    // given
    matcher = newMatcher("/{foo}/{bar}", "GET");

    // when
    matchResult = matcher.match("GET", "/foo/bar");

    // then
    assertThat(matchResult)
        .isNotNull()
        .includes(
          entry("foo", "foo"),
          entry("bar", "bar"));
  }

  @Test
  public void shouldExtractNamedMatchAllUriPart() {

    // given
    matcher = newMatcher("/{foo}/{bar:.*}", "GET");

    // when
    matchResult = matcher.match("GET", "/foo/bar/asdf/asd");

    // then
    assertThat(matchResult)
        .isNotNull()
        .includes(
          entry("bar", "bar/asdf/asd"));
  }

  private RequestFilter newMatcher(String uri, String ... methods) {
    return new RequestFilter(uri, methods);
  }
}
