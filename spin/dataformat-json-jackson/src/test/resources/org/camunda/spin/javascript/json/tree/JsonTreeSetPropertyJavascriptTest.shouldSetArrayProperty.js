node = S(input, "application/json");
var list = [];
list.push("test");
list.push("test2");
node.prop("comment", list);

propertyNode = node.prop("comment");
value = propertyNode.elements().get(1).stringValue();