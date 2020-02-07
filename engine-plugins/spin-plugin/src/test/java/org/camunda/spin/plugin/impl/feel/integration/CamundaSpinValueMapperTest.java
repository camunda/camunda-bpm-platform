/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.spin.plugin.impl.feel.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static scala.jdk.CollectionConverters.ListHasAsScala;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.camunda.feel.impl.spi.CustomValueMapper;
import org.camunda.feel.interpreter.impl.Context;
import org.camunda.feel.interpreter.impl.DefaultValueMapper;
import org.camunda.feel.interpreter.impl.Val;
import org.camunda.feel.interpreter.impl.ValContext;
import org.camunda.feel.interpreter.impl.ValList;
import org.camunda.feel.interpreter.impl.ValString;
import org.camunda.feel.interpreter.impl.ValueMapper;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

public class CamundaSpinValueMapperTest {

  protected static ValueMapper valueMapper;

  @BeforeClass
  public static void setUp() {
    DefaultValueMapper defaultValueMapper = DefaultValueMapper.instance();
    CamundaSpinValueMapper spinValueMapper = new CamundaSpinValueMapper();
    List<CustomValueMapper> mapperList = Arrays.asList(defaultValueMapper, spinValueMapper);
    valueMapper = new ValueMapper.CompositeValueMapper(ListHasAsScala(mapperList).asScala().toList());
  }

  @Test
  public void shouldMapCamundaSpinJSONObjectAsContext() {
    // given
    Map map = new Map.Map2("customer", new ValString("Kermit"), "language", new ValString("en"));
    ValContext context = new ValContext(new Context.StaticContext(map, Map$.MODULE$.empty()));
    SpinJsonNode json = Spin.JSON("{\"customer\": \"Kermit\", \"language\": \"en\"}");

    // when
    Val value = valueMapper.toVal(json);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinJSONarrayAsList() {
    // given
    List<Val> list = Arrays.asList(new ValString("Kermit"), new ValString("Waldo"));
    ValList feelList = (ValList) valueMapper.toVal(list);
    ValContext context = (ValContext) valueMapper.toVal(new Map.Map1("customer", feelList));
    SpinJsonNode json = Spin.JSON("{\"customer\": [\"Kermit\", \"Waldo\"]}");

    // when
    Val value = valueMapper.toVal(json);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapNestedCamundaSpinJSONObjectAsContext() {

    // given
    java.util.Map nestedMap = new HashMap<String, Val>();
    nestedMap.put("city", new ValString("Berlin"));
    nestedMap.put("zipCode", valueMapper.toVal(10961));

    java.util.Map contextMap = new HashMap<String, Val>();
    contextMap.put("customer", new ValString("Kermit"));
    contextMap.put("address", valueMapper.toVal(nestedMap));

    ValContext context = (ValContext) valueMapper.toVal(contextMap);
    SpinJsonNode json = Spin.JSON("{\"customer\": \"Kermit\", \"address\": {\"city\": \"Berlin\", \"zipCode\": 10961}}");

    // when
    Val value = valueMapper.toVal(json);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithAttributes() {
    // given
    java.util.Map xmlInnerMap = new HashMap();
    xmlInnerMap.put("@name", new ValString("Kermit"));
    xmlInnerMap.put("@language", new ValString("en"));
    java.util.Map xmlContextMap = new HashMap();
    xmlContextMap.put("customer", valueMapper.toVal(xmlInnerMap));

    ValContext context = (ValContext) valueMapper.toVal(xmlContextMap);
    SpinXmlElement xml = Spin.XML(" <customer name=\"Kermit\" language=\"en\" /> ");

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithChildObject() {
    // given
    java.util.Map xmlAttrMap = new HashMap();
    xmlAttrMap.put("@city", new ValString("Berlin"));
    xmlAttrMap.put("@zipCode", new ValString("10961"));
    java.util.Map xmlInnerMap = new HashMap();
    xmlInnerMap.put("address", valueMapper.toVal(xmlAttrMap));
    java.util.Map xmlContextMap = new HashMap();
    xmlContextMap.put("customer", valueMapper.toVal(xmlInnerMap));

    ValContext context = (ValContext) valueMapper.toVal(xmlContextMap);
    SpinXmlElement xml = Spin.XML("<customer><address city=\"Berlin\" zipCode=\"10961\" /></customer>");

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithListOfChildObjects() {
    // given
    SpinXmlElement xml = Spin.XML("<data>" +
                                          "<customer name=\"Kermit\" language=\"en\" />" +
                                          "<customer name=\"John\" language=\"de\" />" +
                                          "<provider name=\"Foobar\" />" +
                                         "</data>");

    java.util.Map xmlProviderAttrMap = new HashMap();
    xmlProviderAttrMap.put("@name", new ValString("Foobar"));

    java.util.Map xmlCustomerAttrMap1 = new HashMap();
    xmlCustomerAttrMap1.put("@name", new ValString("Kermit"));
    xmlCustomerAttrMap1.put("@language", new ValString("en"));

    java.util.Map xmlCustomerAttrMap2 = new HashMap();
    xmlCustomerAttrMap2.put("@name", new ValString("John"));
    xmlCustomerAttrMap2.put("@language", new ValString("de"));

    java.util.Map xmlInnerMap = new HashMap();
    xmlInnerMap.put("provider", valueMapper.toVal(xmlProviderAttrMap));
    xmlInnerMap.put("customer", valueMapper.toVal(Arrays.asList(xmlCustomerAttrMap1, xmlCustomerAttrMap2)));

    java.util.Map xmlContextMap = new HashMap();
    xmlContextMap.put("data", valueMapper.toVal(xmlInnerMap));

    ValContext context = (ValContext) valueMapper.toVal(xmlContextMap);

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithContent() {
    // given
    SpinXmlElement xml = Spin.XML("<customer>Kermit</customer>");

    java.util.Map xmlInnerMap = new HashMap();
    xmlInnerMap.put("$content", new ValString("Kermit"));

    java.util.Map xmlContextMap = new HashMap();
    xmlContextMap.put("customer", valueMapper.toVal(xmlInnerMap));

    ValContext context = (ValContext) valueMapper.toVal(xmlContextMap);

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithoutContent() {
    // given
    SpinXmlElement xml = Spin.XML("<customer />");
    ValContext context = (ValContext) valueMapper.toVal(Collections.singletonMap("customer", null));

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }

  @Test
  public void shouldMapCamundaSpinXMLObjectWithPrefix() {
    // given
    SpinXmlElement xml = Spin.XML("<data xmlns:p=\"http://www.example.org\">" +
                                          "<p:customer p:name=\"Kermit\" language=\"en\" />" +
                                        "</data>");

    java.util.Map xmlAttrMap = new HashMap();
    xmlAttrMap.put("@p$name", new ValString("Kermit"));
    xmlAttrMap.put("@language", new ValString("en"));

    java.util.Map xmlInnerMap = new HashMap();
    xmlInnerMap.put("p$customer", valueMapper.toVal(xmlAttrMap));
    xmlInnerMap.put("@xmlns$p", new ValString("http://www.example.org"));

    java.util.Map xmlContextMap = new HashMap();
    xmlContextMap.put("data", valueMapper.toVal(xmlInnerMap));

    ValContext context = (ValContext) valueMapper.toVal(xmlContextMap);

    // when
    Val value = valueMapper.toVal(xml);

    // then
    assertThat(value).isEqualTo(context);
  }
}
