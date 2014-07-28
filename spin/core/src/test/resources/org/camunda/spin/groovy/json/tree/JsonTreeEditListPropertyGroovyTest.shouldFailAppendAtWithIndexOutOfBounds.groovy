package org.camunda.spin.groovy.json.tree

node = JSON(input);
currencies = node.prop("orderDetails").prop("currencies");

currencies.appendAt(6, "test1");
