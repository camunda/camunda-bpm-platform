var node = JSON(input);
var currencies = node.prop("orderDetails").prop("currencies");

currencies.insertAt(-6, "test1");
