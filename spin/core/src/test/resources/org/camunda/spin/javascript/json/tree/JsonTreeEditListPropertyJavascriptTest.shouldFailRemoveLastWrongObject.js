var node = JSON(input);
var customers = node.prop("customers");

customers.removeLast(new Date());
