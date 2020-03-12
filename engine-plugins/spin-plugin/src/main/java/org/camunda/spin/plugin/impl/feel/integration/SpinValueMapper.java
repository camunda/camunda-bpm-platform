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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.camunda.feel.syntaxtree.Val;
import org.camunda.feel.syntaxtree.ValString;
import org.camunda.feel.valuemapper.JavaCustomValueMapper;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.SpinXmlNode;


public class SpinValueMapper extends JavaCustomValueMapper {

  @Override
  public Optional<Val> toValue(Object x, Function<Object, Val> innerValueMapper) {
    if (x instanceof SpinJsonNode) {
      SpinJsonNode node = (SpinJsonNode) x;
      return Optional.of(this.spinJsonToVal(node, innerValueMapper));

    } else if (x instanceof SpinXmlElement) {
      SpinXmlElement element = (SpinXmlElement) x;
      return Optional.of(this.spinXmlToVal(element, innerValueMapper));

    } else {
      return Optional.empty();

    }
  }

  @Override
  public Optional<Object> unpackValue(Val value, Function<Val, Object> innerValueMapper) {
    return Optional.empty();
  }

  @Override
  public int priority() {
    return 30;
  }

  protected Val spinJsonToVal(SpinJsonNode node, Function<Object, Val> innerValueMapper) {
    if (node.isObject()) {
      Map pairs = node.fieldNames()
          .stream()
          .collect(toMap(field -> field,
                         field -> spinJsonToVal(node.prop(field), innerValueMapper)));
      return innerValueMapper.apply(pairs);

    } else if (node.isArray()) {
      List<Val> values = node.elements()
          .stream()
          .map(e -> spinJsonToVal(e, innerValueMapper)).collect(toList());
      return innerValueMapper.apply(values);

    } else if (node.isNull()) {
      return innerValueMapper.apply(null);

    } else {
      return innerValueMapper.apply(node.value());

    }
  }

  protected Val spinXmlToVal(SpinXmlElement element, Function<Object, Val> innerValueMapper) {
    String name = nodeName(element);
    Val value = spinXmlElementToVal(element, innerValueMapper);
    Map<String, Object> map = Collections.singletonMap(name, value);

    return innerValueMapper.apply(map);
  }

  protected Val spinXmlElementToVal(final SpinXmlElement e,
                                    Function<Object, Val> innerValueMapper) {
    Map<String, Object> membersMap = new HashMap<>();
    String content = e.textContent().trim();
    if (!content.isEmpty()) {
      membersMap.put("$content", new ValString(content));
    }

    Map<String, ValString> attributes = e.attrs()
        .stream()
        .collect(toMap(this::spinXmlAttributeToKey, attr -> new ValString(attr.value())));
    if (!attributes.isEmpty()) {
      membersMap.putAll(attributes);
    }

    Map<String, Val> childrenMap = e.childElements().stream()
     .collect(
       groupingBy(
         this::nodeName,
         mapping(el -> spinXmlElementToVal(el, innerValueMapper), toList())
                 ))
     .entrySet().stream()
       .collect(toMap(Map.Entry::getKey, entry -> {
         List<Val> valList = entry.getValue();
         if (!valList.isEmpty() && valList.size() > 1) {
           return innerValueMapper.apply(valList);
         } else {
           return valList.get(0);
         }
       }));
    membersMap.putAll(childrenMap);

    if (membersMap.isEmpty()) {
      return innerValueMapper.apply(null);
    } else {
      return innerValueMapper.apply(membersMap);
    }
  }

  protected String spinXmlAttributeToKey(SpinXmlAttribute attribute) {
    return "@" + nodeName(attribute);
  }

  protected String nodeName(SpinXmlNode n) {
    String prefix = n.prefix();
    String name = n.name();
    return (prefix != null && !prefix.isEmpty())? prefix + "$" + name : name;
  }
}

