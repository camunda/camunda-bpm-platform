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
package org.camunda.spin;

import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.S;

import org.camunda.spin.spi.DataFormat;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinCreateTest {

  @Test
  public void shouldFailForNonExistingDataFormat() {
    try {
      S("{}", "a non-existing format");
      fail("expected exception");
    } catch (IllegalArgumentException e) {
      // happy path
    }

    try {
      DataFormat<?> dataFormat = null;
      S("{}", dataFormat);
      fail("expected exception");
    } catch (IllegalArgumentException e) {
      // happy path
    }
  }
}
