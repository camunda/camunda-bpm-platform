'use strict';

module.exports = function(textPromise, text) {
  textPromise.then(function(resolvedText) {
    expect(resolvedText.toLowerCase()).to.eql(text.toLowerCase());
  });
};
