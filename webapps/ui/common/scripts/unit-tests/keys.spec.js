'use strict';

var chai = require('chai');
var expect = chai.expect;
var getKeys = require('../util/get-keys');

describe('common/utils getKeys', function() {
  var obj;

  beforeEach(function() {
    obj = Object.create({
      c: 3,
      d: 4
    });

    obj.a = 1;
    obj.b = 2;
  });

  it('should return object own keys', function() {
    var keys = getKeys(obj);

    expect(keys).to.deep.eql(['a', 'b']);
  });

  it('should include prototype properties when includePrototype is true', function() {
    var keys = getKeys(obj, true);

    expect(keys).to.deep.eql(['a', 'b', 'c', 'd']);
  });
});
