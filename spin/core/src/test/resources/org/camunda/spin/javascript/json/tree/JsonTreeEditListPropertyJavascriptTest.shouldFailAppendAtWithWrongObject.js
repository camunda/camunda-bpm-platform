var node = JSON(input);
var customers = node.prop("customers");
customers.appendAt(1, new Date());