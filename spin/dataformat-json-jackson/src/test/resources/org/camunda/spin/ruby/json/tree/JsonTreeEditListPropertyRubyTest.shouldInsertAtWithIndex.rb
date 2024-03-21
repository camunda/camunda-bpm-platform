node = S($input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

$oldSize = currencies.elements().size()
$oldPosition = currencies.indexOf("dollar")

currencies.insertAt(1, "test1")

$newSize = currencies.elements().size()
$newPosition = currencies.indexOf("dollar")
$value = currencies.elements().get(1).stringValue()
