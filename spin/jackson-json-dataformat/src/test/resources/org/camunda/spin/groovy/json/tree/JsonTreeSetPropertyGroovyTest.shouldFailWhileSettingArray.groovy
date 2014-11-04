package org.camunda.spin.groovy.json.tree

node = JSON(input);
Date date = new Date();
def list = [date]

node.prop("comment", list);