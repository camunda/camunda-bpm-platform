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
package org.camunda.spin.xml.dom.format.spi;

import static org.assertj.core.api.Fail.fail;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.xml.SpinXmlDataFormatException;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.mapping.Customer;
import org.junit.After;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class JaxBContextProviderTest {

  /**
   * This test uses a dataformat with a JAXBContext that cannot resolve any classes.
   * Thus, it is expected that mapping an object to XML using this context fails.
   */
  @Test
  public void testCustomJaxBProvider() {

    Object objectToConvert = new Customer();

    // using the default jaxb context provider for conversion should work
    SpinXmlElement spinWrapper = Spin.XML(objectToConvert);
    spinWrapper.writeToWriter(new StringWriter());

    // using the custom jaxb context provider should fail with a JAXBException
    ((DomXmlDataFormat) DataFormats.xml()).setJaxBContextProvider(new EmptyContextProvider());
    try {
      spinWrapper = Spin.XML(objectToConvert);
      spinWrapper.writeToWriter(new StringWriter());
    } catch (SpinXmlDataFormatException e) {

      // assert that there is a jaxb exception somewhere in the exception hierarchy
      Set<Throwable> processedExceptions = new HashSet<Throwable>();
      while (!processedExceptions.contains(e.getCause()) && e.getCause() != null) {
        if (e.getCause() instanceof JAXBException) {
          // happy path
          return;
        }

        processedExceptions.add(e.getCause());
      }

      fail("expected a JAXBException in the cause hierarchy of the spin exception");
    }

  }

  @After
  public void tearDown() {
    // reset jaxb context provider
    ((DomXmlDataFormat) DataFormats.xml()).setJaxBContextProvider(DomXmlDataFormat.defaultJaxBContextProvider());
  }
}
