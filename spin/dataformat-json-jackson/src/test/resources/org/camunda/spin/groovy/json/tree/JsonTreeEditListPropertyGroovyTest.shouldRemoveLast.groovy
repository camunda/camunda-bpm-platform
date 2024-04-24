package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");

oldSize = node.elements().size();
oldValue = node.elements().get(oldSize - 1).stringValue();

node.removeLast("test");

newSize = node.elements().size();
newValue = node.elements().get(newSize - 1).stringValue();
