var node = S(input, "application/json");
var customers = node.prop("customers");

customers.insertAt(1, null);