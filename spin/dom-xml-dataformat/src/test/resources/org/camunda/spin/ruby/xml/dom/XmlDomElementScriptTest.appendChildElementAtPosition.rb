child1 = S($child)
child2 = S($child)
child3 = S($child)

$element = S($input)
childs = $element.childElements()
firstChild = childs.get(0)
lastChild = childs.get(childs.size() - 1)

$element.appendBefore(child1, firstChild)
$element.appendAfter(child2, firstChild)
$element.appendAfter(child3, lastChild)
