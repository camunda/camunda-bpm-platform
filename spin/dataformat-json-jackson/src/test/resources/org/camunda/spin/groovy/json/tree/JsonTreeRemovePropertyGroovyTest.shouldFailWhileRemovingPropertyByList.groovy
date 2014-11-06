package org.camunda.spin.groovy.json.tree

node = JSON(input)
def list = ["order", "comment"]
node.deleteProp(list)