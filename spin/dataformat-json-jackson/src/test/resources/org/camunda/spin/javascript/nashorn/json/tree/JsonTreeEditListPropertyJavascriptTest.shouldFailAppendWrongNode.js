var node = S(input, "application/json");
var customers = node.prop("customers");

customers.append(new Date());