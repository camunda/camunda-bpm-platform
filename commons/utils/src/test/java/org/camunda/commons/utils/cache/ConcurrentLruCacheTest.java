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
package org.camunda.commons.utils.cache;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentLruCacheTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ConcurrentLruCache<String, String> cache;

  @Before
  public void createCache() {
    cache = new ConcurrentLruCache<String, String>(3);
  }

  @Test
  public void getEntryWithNotExistingKey() {
    assertThat(cache.get("not existing")).isNull();
  }

  @Test
  public void getEntry() {
    cache.put("a", "1");

    assertThat(cache.size()).isEqualTo(1);
    assertThat(cache.get("a")).isEqualTo("1");
  }

  @Test
  public void overrideEntry() {
    cache.put("a", "1");
    cache.put("a", "2");

    assertThat(cache.size()).isEqualTo(1);
    assertThat(cache.get("a")).isEqualTo("2");
  }

  @Test
  public void removeLeastRecentlyInsertedEntry() {
    cache.put("a", "1");
    cache.put("b", "2");
    cache.put("c", "3");
    cache.put("d", "4");

    assertThat(cache.size()).isEqualTo(3);
    assertThat(cache.get("a")).isNull();
    assertThat(cache.get("b")).isEqualTo("2");
    assertThat(cache.get("c")).isEqualTo("3");
    assertThat(cache.get("d")).isEqualTo("4");
  }

  @Test
  public void removeLeastRecentlyUsedEntry() {
    cache.put("a", "1");
    cache.put("b", "2");
    cache.put("c", "3");

    cache.get("a");
    cache.get("b");

    cache.put("d", "4");

    assertThat(cache.size()).isEqualTo(3);
    assertThat(cache.get("c")).isNull();
    assertThat(cache.get("a")).isEqualTo("1");
    assertThat(cache.get("b")).isEqualTo("2");
    assertThat(cache.get("d")).isEqualTo("4");
  }

  @Test
  public void clearCache() {
    cache.put("a", "1");

    cache.clear();
    assertThat(cache.size()).isEqualTo(0);
    assertThat(cache.get("a")).isNull();
  }

  @Test
  public void failToInsertInvalidKey() {
    thrown.expect(NullPointerException.class);

    cache.put(null, "1");
  }

  @Test
  public void failToInsertInvalidValue() {
    thrown.expect(NullPointerException.class);

    cache.put("a", null);
  }

  @Test
  public void failToCreateCacheWithInvalidCapacity() {
    thrown.expect(IllegalArgumentException.class);

    new ConcurrentLruCache<String, String>(-1);
  }

  @Test
  public void removeElementInEmptyCache() {

    // given
    cache.clear();

    // when
    cache.remove("123");

    // then
    assertThat(cache.isEmpty()).isTrue();
  }

  @Test
  public void removeNoneExistingKeyInCache(){
    //given
    cache.put("a", "1");
    cache.put("b", "2");
    cache.put("c", "3");

    // when
    cache.remove("d");

    // then
    assertThat(cache.get("a")).isEqualTo("1");
    assertThat(cache.get("b")).isEqualTo("2");
    assertThat(cache.get("c")).isEqualTo("3");
  }

  @Test
  public void removeAllElements() {
    // given
    cache.put("a", "1");
    cache.put("b", "2");
    cache.put("c", "3");

    // when
    cache.remove("a");
    cache.remove("b");
    cache.remove("c");

    // then
    assertThat(cache.isEmpty()).isTrue();
  }


}
