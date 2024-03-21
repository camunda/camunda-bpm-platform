node = S($input, "application/json")
node.prop("comment", "42!")

$propertyNode = node.prop("comment")
$value = $propertyNode.stringValue()