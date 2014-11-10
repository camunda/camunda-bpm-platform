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

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.camunda.spin.DataFormats;
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

  @SuppressWarnings("rawtypes")
  protected ServiceLoader<DataFormatConfigurator> mockConfiguratorLoader;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    mockStatic(ServiceLoader.class);

    mockServiceLoader = mock(ServiceLoader.class);
    when(ServiceLoader.load(Matchers.eq(DataFormatProvider.class), Matchers.any(ClassLoader.class)))
      .thenReturn(mockServiceLoader);

    mockConfiguratorLoader = mock(ServiceLoader.class);
    when(ServiceLoader.load(Matchers.eq(DataFormatConfigurator.class), Matchers.any(ClassLoader.class)))
      .thenReturn(mockConfiguratorLoader);
  }

  @Test
  @PrepareForTest( { DataFormats.class })
  public void testCustomDataFormatProvider() {
    // given a custom data format provider that is returned by the service loader API
    mockProviders(new CustomDataFormatProvider());
    mockConfigurators();

    // when the custom data format is requested
    DataFormat<?> customDataFormat = DataFormats.getDataFormat(CustomDataFormatProvider.NAME);

    // then it should be properly returned
    assertThat(customDataFormat).isNotNull();
    assertThat(customDataFormat).isSameAs(CustomDataFormatProvider.DATA_FORMAT);
  }


  @Test
  @PrepareForTest( { DataFormats.class })
  public void testConfigureDataFormat() {
    // given a custom data format provider that is returned by the service loader API
    mockProviders(new CustomDataFormatProvider());
    mockConfigurators(new ExampleCustomDataFormatConfigurator());

    DataFormat<?> format = DataFormats.getDataFormat(CustomDataFormatProvider.NAME);
    assertThat(format).isSameAs(CustomDataFormatProvider.DATA_FORMAT);

    // then the configuration was applied
    ExampleCustomDataFormat customFormat = (ExampleCustomDataFormat) format;
    assertThat(customFormat.getProperty()).isEqualTo(ExampleCustomDataFormatConfigurator.UPDATED_PROPERTY);
  }

  protected void mockProviders(final DataFormatProvider... providers) {
    when(mockServiceLoader.iterator()).thenAnswer(new Answer<Iterator<DataFormatProvider>>() {

      @Override
      public Iterator<DataFormatProvider> answer(InvocationOnMock invocation) throws Throwable {
        return Arrays.asList(providers).iterator();
      }
    });
  }

  protected void mockConfigurators(final DataFormatConfigurator<?>... configurators) {
    when(mockConfiguratorLoader.iterator()).thenAnswer(new Answer<Iterator<DataFormatConfigurator<?>>>() {

      @Override
      public Iterator<DataFormatConfigurator<?>> answer(InvocationOnMock invocation) throws Throwable {
        return Arrays.asList(configurators).iterator();
      }
    });
  }
}
