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

package org.camunda.spin.test;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
public abstract class ScriptRuleTest extends ScriptTest {

  @Test
  @Script
  public void shouldSetFoo() {
    Object foo = script.getVariable("foo");
    assertThat(foo).isNotNull();

    Object bar = script.getVariable("bar");
    assertThat(bar).isNull();
  }

  @Test
  @Script
  public void shouldSetBar() {
    Object foo = script.getVariable("foo");
    assertThat(foo).isNull();

    Object bar = script.getVariable("bar");
    assertThat(bar).isNotNull();
  }

}
