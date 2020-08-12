import thirdpartylib.MultiplyTwo as OrigMultiplyTwo

class MultiplyTwo extends OrigMultiplyTwo {
    def multiply(def value) {
        return value * 2 // fixed here
    }
}

// nothing to change below here
def multiplylib = new MultiplyTwo()

// assert passes as well
assert 4 == new MultiplyTwo().multiply(2)