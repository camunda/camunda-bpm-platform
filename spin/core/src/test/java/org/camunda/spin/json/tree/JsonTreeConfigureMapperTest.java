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
package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class JsonTreeConfigureMapperTest {

  @Test
  public void shouldPassConfigurationToNewInstance() {
    DateFormat dateFormat = new SimpleDateFormat();

    JsonJacksonTreeDataFormat jsonDataFormat = new JsonJacksonTreeDataFormat();
    jsonDataFormat.mapper().config("aKey", "aValue");
    jsonDataFormat.mapper().dateFormat(dateFormat);
    jsonDataFormat.mapper().enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);

    JsonJacksonTreeDataFormat jsonDataFormatInstance =
        jsonDataFormat.newInstance().mapper().config("anotherKey", "anotherValue").done();

    assertThat(jsonDataFormat.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormat.mapper().getValue("anotherKey")).isNull();

    assertThat(jsonDataFormatInstance.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormatInstance.mapper().getValue("anotherKey")).isEqualTo("anotherValue");

    assertThat(jsonDataFormatInstance.mapper().getDateFormat()).isSameAs(dateFormat);
    assertThat(jsonDataFormatInstance.mapper().getDefaultTyping()).isEqualTo(DefaultTyping.JAVA_LANG_OBJECT);
    assertThat(jsonDataFormatInstance.mapper().getDefaultTypingFormat()).isEqualTo(As.PROPERTY);
  }

}
