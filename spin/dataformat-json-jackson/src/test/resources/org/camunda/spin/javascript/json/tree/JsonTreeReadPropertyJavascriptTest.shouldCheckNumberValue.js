node = S(input, "application/json");

property1 = node.prop("order");
property2 = node.prop("id");
property3 = node.prop("customers");
property4 = node.prop("orderDetails");
property5 = node.prop("active");

value1 = property1.isNumber();
value2 = property2.isNumber();
value3 = property3.isNumber();
value4 = property4.isNumber();
value5 = property5.isNumber();