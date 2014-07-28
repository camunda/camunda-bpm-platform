var node = JSON(input);
var currencies = node.prop("orderDetails").prop("currencies");

currencies.appendAt(6, "test1");
