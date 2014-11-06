node = JSON($input)
node.prop("comment", false)

$propertyNode = node.prop("comment")
$value = $propertyNode.boolValue()