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
package org.camunda.bpm.dmn.engine.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.dmn.engine.test.DmnEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the build-in {@link DataTypeTransformer}s.
 *
 * @author Philipp Ossler
 */
public class DataTypeTransformerTest extends DmnDecisionTest {

  @Rule
  public DmnEngineRule dmnEngineRule = new DmnEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected DataTypeTransformerFactory factory;

  @Before
  public void setup() {
    factory = dmnEngineRule.getEngine().getConfiguration().getDataTypeTransformerFactory();
  }

  @Test
  public void customType() {
    // by default, the factory should return a transformer for unsupported type
    // that just box the value into an untyped value
    assertThat(factory.getTransformerForType("custom").transform("42"), is(Variables.untypedValue("42")));
  }

  @Test
  public void stringType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("string");

    assertThat(typeTransformer.transform("abc"), is((TypedValue) Variables.stringValue("abc")));
    assertThat(typeTransformer.transform(true), is((TypedValue) Variables.stringValue("true")));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.stringValue("4")));
    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.stringValue("2")));
    assertThat(typeTransformer.transform(4.2), is((TypedValue) Variables.stringValue("4.2")));
  }

  @Test
  public void booleanType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("boolean");

    assertThat(typeTransformer.transform(true), is((TypedValue) Variables.booleanValue(true)));
    assertThat(typeTransformer.transform(false), is((TypedValue) Variables.booleanValue(false)));

    assertThat(typeTransformer.transform("true"), is((TypedValue) Variables.booleanValue(true)));
    assertThat(typeTransformer.transform("false"), is((TypedValue) Variables.booleanValue(false)));
  }

  @Test
  public void invalidStringValueForBooleanType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("boolean");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("NaB");
  }

  @Test
  public void integerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.integerValue(4)));
    assertThat(typeTransformer.transform("4"), is((TypedValue) Variables.integerValue(4)));
    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.integerValue(2)));
    assertThat(typeTransformer.transform(4.0), is((TypedValue) Variables.integerValue(4)));

    assertThat(typeTransformer.transform(Integer.MIN_VALUE), is((TypedValue) Variables.integerValue(Integer.MIN_VALUE)));
    assertThat(typeTransformer.transform(Integer.MAX_VALUE), is((TypedValue) Variables.integerValue(Integer.MAX_VALUE)));
  }

  @Test
  public void invalidStringValueForIntegerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("4.2");
  }

  @Test
  public void invalidDoubleValueForIntegerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(4.2);
  }

  @Test
  public void invalidLongValueForIntegerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Long.MAX_VALUE);
  }

  @Test
  public void invalidIntegerMinValueForIntegerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Integer.MIN_VALUE - 1L);
  }

  @Test
  public void invalidIntegerMaxValueForIntegerType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Integer.MAX_VALUE + 1L);
  }

  @Test
  public void longType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("long");

    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.longValue(2L)));
    assertThat(typeTransformer.transform("2"), is((TypedValue) Variables.longValue(2L)));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.longValue(4L)));
    assertThat(typeTransformer.transform(4.0), is((TypedValue) Variables.longValue(4L)));

    assertThat(typeTransformer.transform(Long.MIN_VALUE), is((TypedValue) Variables.longValue(Long.MIN_VALUE)));
    assertThat(typeTransformer.transform(Long.MAX_VALUE), is((TypedValue) Variables.longValue(Long.MAX_VALUE)));
  }

  @Test
  public void invalidStringValueForLongType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("4.2");
  }

  @Test
  public void invalidDoubleValueForLongType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(4.2);
  }

  @Test
  public void invalidDoubleMinValueForLongType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Double.MIN_VALUE);
  }

  @Test
  public void doubleType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("double");

    assertThat(typeTransformer.transform(4.2), is((TypedValue) Variables.doubleValue(4.2)));
    assertThat(typeTransformer.transform("4.2"), is((TypedValue) Variables.doubleValue(4.2)));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.doubleValue(4.0)));
    assertThat(typeTransformer.transform(4L), is((TypedValue) Variables.doubleValue(4.0)));

    assertThat(typeTransformer.transform(Double.MIN_VALUE), is((TypedValue) Variables.doubleValue(Double.MIN_VALUE)));
    assertThat(typeTransformer.transform(Double.MAX_VALUE), is((TypedValue) Variables.doubleValue(Double.MAX_VALUE)));
    assertThat(typeTransformer.transform(-Double.MAX_VALUE), is((TypedValue) Variables.doubleValue(-Double.MAX_VALUE)));
    assertThat(typeTransformer.transform(Long.MAX_VALUE), is((TypedValue) Variables.doubleValue(Double.valueOf(Long.MAX_VALUE))));
  }

  @Test
  public void invalidStringValueForDoubleType() {
    DataTypeTransformer typeTransformer = factory.getTransformerForType("double");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("NaD");
  }

}
