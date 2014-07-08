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

package org.camunda.commons.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.commons.utils.StringUtil.isExpression;
import static org.camunda.commons.utils.StringUtil.join;
import static org.camunda.commons.utils.StringUtil.split;

/**
 * @author Sebastian Menski
 */
public class StringUtilTest {

  @Test
  public void testExpressionDetection() {
    assertThat(isExpression("${test}")).isTrue();
    assertThat(isExpression("${a(b,c)}")).isTrue();
    assertThat(isExpression("${ test }")).isTrue();
    assertThat(isExpression(" ${test} ")).isTrue();
    assertThat(isExpression(" \n${test} ")).isTrue();

    assertThat(isExpression("#{test}")).isTrue();
    assertThat(isExpression("#{a(b,c)}")).isTrue();
    assertThat(isExpression("#{ test }")).isTrue();
    assertThat(isExpression(" #{test} ")).isTrue();
    assertThat(isExpression(" \n#{test} ")).isTrue();

    assertThat(isExpression("test")).isFalse();
    assertThat(isExpression("    test")).isFalse();
    assertThat(isExpression("{test}")).isFalse();
    assertThat(isExpression("(test)")).isFalse();
    assertThat(isExpression("")).isFalse();
    assertThat(isExpression(null)).isFalse();
  }

  @Test
  public void testStringSplit() {
    assertThat(split("a,b,c", ",")).hasSize(3).containsExactly("a", "b", "c");
    assertThat(split("aaaxbaaxc", "a{2}x")).hasSize(3).containsExactly("a", "b", "c");
    assertThat(split(null, ",")).isNull();
    assertThat(split("abc", ",")).hasSize(1).containsExactly("abc");
    assertThat(split("a,b,c", null)).hasSize(1).containsExactly("a,b,c");
  }

  @Test
  public void testStringJoin() {
    assertThat(join(",", "a", "b", "c")).isEqualTo("a,b,c");
    assertThat(join(", ", "a", "b", "c")).isEqualTo("a, b, c");
    assertThat(join(null, "a", "b", "c")).isEqualTo("abc");
    assertThat(join(",", "")).isEqualTo("");
    assertThat(join(null, (String[]) null)).isNull();
    assertThat(join("aax", "a", "b", "c")).isEqualTo("aaaxbaaxc");
  }

}
