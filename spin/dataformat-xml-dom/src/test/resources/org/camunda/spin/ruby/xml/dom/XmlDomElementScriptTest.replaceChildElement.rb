$element = S($input) unless $input.nil?

if $newChild.nil?
  newChild = $newChild
else
  newChild = S($newChild)
end

$element.replaceChild($existingChild, newChild)
