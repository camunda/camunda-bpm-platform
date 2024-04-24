if $newElement.nil?
  newElement = $newElement
else
  newElement = S($newElement)
end

$element = $oldElement.replace(newElement)
