node = S($input, "application/json")

$oldValue = node.prop("order")

json_object = {
    "name" => "test",
    "comment" => "42!"
}

node.prop("order", json_object)
$newValue = node.prop("order")