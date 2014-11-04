var node = JSON(input);
var customers = node.prop("customers");
customers.insertAt(1, new Date());