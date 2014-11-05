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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.DataFormats.json;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.impl.util.SpinIoUtil.stringAsReader;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_EMPTY_STRING;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_INVALID_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

import java.io.Reader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.SpinDataFormatException;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 */
public class JsonTreeCreateTest {

  @Test
  public void shouldCreateForString() {
    SpinJsonNode json = JSON(EXAMPLE_JSON);
    assertThat(json).isNotNull();

    json = S(EXAMPLE_JSON, json());
    assertThat(json).isNotNull();

    json = S(EXAMPLE_JSON, DataFormats.JSON_DATAFORMAT_NAME);
    assertThat(json).isNotNull();

    json = S(EXAMPLE_JSON);
    assertThat(json).isNotNull();
  }

  @Test
  public void shouldCreateObjectDeclaredInput() {
    Object input = EXAMPLE_JSON;
    SpinJsonNode jsonNode = JSON(input);
    assertThat(jsonNode.prop("order")).isNotNull();
  }

  @Test
  public void shouldCreateForReader() {
    SpinJsonNode json = JSON(stringAsReader(EXAMPLE_JSON));
    assertThat(json).isNotNull();

    json = S(stringAsReader(EXAMPLE_JSON), json());
    assertThat(json).isNotNull();

    json = S(stringAsReader(EXAMPLE_JSON), DataFormats.JSON_DATAFORMAT_NAME);
    assertThat(json).isNotNull();

    json = S(stringAsReader(EXAMPLE_JSON));
    assertThat(json).isNotNull();
  }

  @Test
  public void shouldBeIdempotent() {
    SpinJsonNode json = JSON(EXAMPLE_JSON);
    assertThat(json).isEqualTo(JSON(json));
    assertThat(json).isEqualTo(S(json, json()));
    assertThat(json).isEqualTo(S(json, DataFormats.JSON_DATAFORMAT_NAME));
    assertThat(json).isEqualTo(S(json));
  }

  @Test
  public void shouldFailForNull() {
    SpinJsonNode jsonNode = null;

    try {
      JSON(jsonNode);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(jsonNode, json());
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(jsonNode);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    Reader reader = null;

    try {
      JSON(reader);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(reader, json());
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(reader);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    String inputString = null;

    try {
      JSON(inputString);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(inputString, json());
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(inputString, DataFormats.JSON_DATAFORMAT_NAME);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(inputString);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForInvalidJson() {
    try {
      JSON(EXAMPLE_INVALID_JSON);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_INVALID_JSON, json());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_INVALID_JSON, DataFormats.JSON_DATAFORMAT_NAME);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_INVALID_JSON);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForEmptyString() {
    try {
      JSON(EXAMPLE_EMPTY_STRING);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_EMPTY_STRING, json());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_EMPTY_STRING, DataFormats.JSON_DATAFORMAT_NAME);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_EMPTY_STRING);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForEmptyReader() {
    try {
      JSON(stringAsReader(EXAMPLE_EMPTY_STRING));
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(stringAsReader(EXAMPLE_EMPTY_STRING), json());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(stringAsReader(EXAMPLE_EMPTY_STRING));
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldCreateForBoolean() {
    SpinJsonNode node = JSON("false");
    assertThat(node.isBoolean()).isTrue();
    assertThat(node.isValue()).isTrue();
    assertThat(node.boolValue()).isFalse();
  }

  @Test
  public void shouldCreateForNumber() {
    SpinJsonNode node = JSON("42");
    assertThat(node.isNumber()).isTrue();
    assertThat(node.isValue()).isTrue();
    assertThat(node.numberValue()).isEqualTo(42);
  }

  @Test
  public void shouldCreateForPrimitiveString() {
    SpinJsonNode node = JSON("\"a String\"");
    assertThat(node.isString()).isTrue();
    assertThat(node.isValue()).isTrue();
    assertThat(node.stringValue()).isEqualTo("a String");
  }

  @Test
  public void shouldFailForUnescapedString() {
    try {
      JSON("a String");
      fail("expected exception");
    } catch (SpinDataFormatException e) {
      // expected
    }
  }
}
