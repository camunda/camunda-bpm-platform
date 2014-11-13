package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");
currencies = node.prop("orderDetails").prop("currencies");

currencies.insertAt(-6, "test1");
