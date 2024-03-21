jsonNode = S($input, "application/json")

$emptyList = jsonNode.jsonPath("customers[?(@.name == 'Klo')]").elementList()
$nodeList = jsonNode.jsonPath("customers[?(@.name == 'Waldo')]").elementList()