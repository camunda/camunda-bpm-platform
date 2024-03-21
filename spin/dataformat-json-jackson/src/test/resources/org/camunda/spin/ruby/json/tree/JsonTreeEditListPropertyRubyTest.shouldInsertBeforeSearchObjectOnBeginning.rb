node = S($input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

$oldSize = currencies.elements().size()
$oldValue = currencies.elements().get(0).stringValue()

currencies.insertBefore($oldValue, "Test")

$newSize = currencies.elements().size()
$newValue = currencies.elements().get(0).stringValue()
$oldValueOnNewPosition = currencies.elements().get(1).stringValue()
