var node = S(input, "application/json");
var customers = node.prop("customers");

customers.removeLast(new Date());
