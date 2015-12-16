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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class DataFormatsTest {

  @Test
  public void testCustomClassLoaderForInitialization() {
    // when initializing data formats with a class loader
    DataFormats dataFormats = new DataFormats();
    dataFormats.registerDataFormats(DataFormats.class.getClassLoader());

    // then the operation was successful
    Assert.assertEquals(0, dataFormats.getAllAvailableDataFormats().size());

    // note: this checks the existence of API that allows to initialize
    // data formats with a custom class loader; the functionality is actually tested
    // as part of the platform integration tests
  }
}
