$element = S($input) unless $input.nil?

if $child2.nil?
  $element.remove($child)
else
  $element.remove($child, $child2)
end

