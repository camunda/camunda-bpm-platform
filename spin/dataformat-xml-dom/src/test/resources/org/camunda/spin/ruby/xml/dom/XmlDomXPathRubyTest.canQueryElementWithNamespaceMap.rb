map = {
  "a" => "http://camunda.com"
}

$query = S($input).xPath($expression).ns(map)
