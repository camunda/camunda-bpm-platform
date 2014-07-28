package org.camunda.spin.groovy.json.tree

node = JSON(input);
customers = node.prop("customers");
customers.appendAt(1, null);