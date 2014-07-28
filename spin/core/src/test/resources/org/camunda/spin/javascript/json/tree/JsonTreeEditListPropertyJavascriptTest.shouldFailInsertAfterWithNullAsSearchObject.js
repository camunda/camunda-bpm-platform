var node = JSON(input);
var currencies = node.prop("orderDetails").prop("currencies");

currencies.insertAfter(null, "test");