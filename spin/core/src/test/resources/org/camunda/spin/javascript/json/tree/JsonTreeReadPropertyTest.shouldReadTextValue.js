node = JSON(input);
childNode1 = node.prop("customers");
childNode2 = node.prop("orderDetails");
list = childNode1.elements();
customerNode = list.get(0);

property1 = node.prop("order");
property2 = customerNode.prop("name");
property3 = childNode2.prop("article");

value1 = property1.value();
value2 = property2.value();
value3 = property3.value();
