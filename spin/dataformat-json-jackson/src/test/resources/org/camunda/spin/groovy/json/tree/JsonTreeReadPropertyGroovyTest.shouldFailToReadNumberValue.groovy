package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");

property1 = node.prop("active");

value1 = property1.numberValue();