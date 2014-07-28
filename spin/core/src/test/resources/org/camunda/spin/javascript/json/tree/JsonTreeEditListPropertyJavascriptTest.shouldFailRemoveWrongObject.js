var node = JSON(input);
var customers = node.prop("customers");

customers.remove(new Date());
