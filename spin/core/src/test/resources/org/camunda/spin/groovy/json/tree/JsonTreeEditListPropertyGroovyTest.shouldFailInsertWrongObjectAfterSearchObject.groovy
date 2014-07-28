package org.camunda.spin.groovy.json.tree

node = JSON(input);
currencies = node.prop("orderDetails").prop("currencies");

currencies.insertAfter("euro", new Date());
