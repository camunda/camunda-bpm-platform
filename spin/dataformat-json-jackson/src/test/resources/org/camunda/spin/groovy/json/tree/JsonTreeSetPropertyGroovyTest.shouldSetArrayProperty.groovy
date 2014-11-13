node = S(input, "application/json");
list = new ArrayList();
list.push("test");
list.push("test2");
node.prop("comment", list);

propertyNode = node.prop("comment");
value = propertyNode.elements().get(1).stringValue();