node = JSON($input)

testobject = {
    "name" => "test",
    "comment" => "42!"
}

node.prop("comment", testobject)

$propertyNode = node.prop("comment")
$value = $propertyNode.prop("comment").stringValue()