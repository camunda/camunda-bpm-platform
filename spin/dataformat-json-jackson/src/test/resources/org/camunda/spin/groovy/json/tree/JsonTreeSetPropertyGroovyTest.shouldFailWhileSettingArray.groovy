package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");
Date date = new Date();
def list = [date]

node.prop("comment", list);