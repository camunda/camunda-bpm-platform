node = JSON($input)
childNode = node.prop("customers")
list = childNode.elements()

$property1 = list.get(0)
$property2 = list.get(1)

customer1 = $property1.prop("name")
customer2 = $property2.prop("name")

$value1 = customer1.value()
$value2 = customer2.value()

