node = JSON($input)
$childNode = node.prop("orderDetails")

$property = $childNode.prop("article")

$value = $property.stringValue()
