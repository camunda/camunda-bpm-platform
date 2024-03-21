unless $child.nil?
    childElement = S($child)
end

$element = S($input).append(childElement)
childElement.attr('id', 'child')
