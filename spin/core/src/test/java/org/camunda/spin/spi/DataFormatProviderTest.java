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
package org.camunda.spin.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Note: The @RunWith and @PrepareForTest annotations are required by powermock to be able
 * to mock static methods provided by the JDK (i.e. ServiceLoader.load(..) in our case).
 * See https://code.google.com/p/powermock/wiki/MockSystem and
 * https://code.google.com/p/powermock/wiki/MockStatic
 *
 * @author Thorben Lindhauer
 */
@RunWith(PowerMockRunner.class)
public class DataFormatProviderTest {

  protected ServiceLoader<DataFormatProvider> mockServiceLoader;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    mockStatic(ServiceLoader.class);

    mockServiceLoader = mock(ServiceLoader.class);
    when(ServiceLoader.load(Matchers.eq(DataFormatProvider.class), Matchers.any(ClassLoader.class)))
      .thenReturn(mockServiceLoader);
  }

  @Test
  @PrepareForTest( { DataFormats.class })
  public void testCustomDataFormatProvider() {
    // given a custom data format provider that is returned by the service loader API
    final List<DataFormatProvider> customProviders = new ArrayList<DataFormatProvider>();
    customProviders.add(new CustomDataFormatProvider());

    when(mockServiceLoader.iterator()).thenAnswer(new Answer<Iterator<DataFormatProvider>>() {

      @Override
      public Iterator<DataFormatProvider> answer(InvocationOnMock invocation) throws Throwable {
        return customProviders.iterator();
      }
    });

    // when the custom data format is requested
    DataFormat<?> customDataFormat = DataFormats.getInstance().getDataFormatByName(CustomDataFormatProvider.NAME);

    // then it should be properly returned
    assertThat(customDataFormat).isNotNull();
    assertThat(customDataFormat).isInstanceOf(DomXmlDataFormat.class);
  }


  @Test
  @PrepareForTest( { DataFormats.class })
  public void testOverrideBuiltInDataFormat() {
    // given a custom data format provider that is returned by the service loader API
    final List<DataFormatProvider> customProviders = new ArrayList<DataFormatProvider>();
    customProviders.add(new OverrideBuiltinJsonDataFormatProvider());

    when(mockServiceLoader.iterator()).thenAnswer(new Answer<Iterator<DataFormatProvider>>() {

      @Override
      public Iterator<DataFormatProvider> answer(InvocationOnMock invocation) throws Throwable {
        return customProviders.iterator();
      }
    });

    // when the default json data format is requested
    // then the one provided by the provider should be returned
    DataFormat<?> jsonFormat = DataFormats.json();
    assertThat(jsonFormat).isSameAs(OverrideBuiltinJsonDataFormatProvider.DATA_FORMAT);

    jsonFormat = DataFormats.getInstance().getDataFormatByName(DataFormats.JSON_DATAFORMAT_NAME);
    assertThat(jsonFormat).isSameAs(OverrideBuiltinJsonDataFormatProvider.DATA_FORMAT);
  }
}
