//worlds smallest unit testing framework
var Assert = (Assert = function() {
    var AssertException = function(message) {
        this.message = message;
    };

    AssertException.prototype.toString = function() {
        return 'AssertException: ' + this.message;
    };

    this.assert = this.that = function(exp, message) {

        if (!exp) {
            throw new AssertException(message);
        }
    }

    this.equals = function(expected, actual, detail) {
        this.assert(expected == actual, 'Failed asserting that ' + expected + '===' + actual + ' :: ' + detail);
    }

    this.type = function(expectedType, actual) {
            this.assert(expectedType === actual.constructor.name, 'Failed asserting that types match: ' + expectedType + ' ===  ' + actual.constructor.name);
        }
        //end worlds smallest unit testing framework
    return this;
}());