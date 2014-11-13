node = S(input, "application/json");
childNode1 = node.prop("customers");
childNode2 = node.prop("orderDetails");
list = childNode1.elements();
customerNode = list.get(0);

property1 = node.prop("order");
property2 = customerNode.prop("name");
property3 = childNode2.prop("article");

value1 = property1.stringValue();
value2 = property2.stringValue();
value3 = property3.stringValue();
