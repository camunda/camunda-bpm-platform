if (input) {
    element = S(input);
}

if (newChild) {
    newChild = S(newChild);
}

element.replaceChild(existingChild, newChild);

