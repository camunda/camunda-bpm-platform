'use strict';

var chai = require('chai');
var sinon = require('sinon');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common promisify service', function() {
  var promisify;
  var $rootScope;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function(_promisify_, _$rootScope_) {
    promisify = _promisify_;
    $rootScope = _$rootScope_;
  }));

  describe('promisifyObject', function() {
    it('should not change non-function properties of object', function() {
      var obj = {
        a: 1,
        b: 'b'
      };
      var promisifiedObj = promisify.promisifyObject(obj);

      expect(promisifiedObj.a).to.eql(1);
      expect(promisifiedObj.b).to.eql('b');
    });

    it('should not deeply promisify', function() {
      var obj = {
        a: {
          targetFn: targetFn
        }
      };
      var promisifiedObj = promisify.promisifyObject(obj);
      var spy = sinon.spy();

      expect(promisifiedObj.a.targetFn).to.eql(targetFn);

      promisifiedObj.a.targetFn(12, spy);

      expect(spy.calledOnce).to.eql(true);
      expect(spy.calledWith(null, 12)).to.eql(true);
    });

    it('should promisify methods', function(done) {
      var obj = promisify.promisifyObject({
        targetFn: targetFn
      });

      obj
        .targetFn(12)
        .then(function(result) {
          expect(result).to.eql(12);

          done();
        });

      $rootScope.$digest();
    });

    it('should promisify methods from prototype', function(done) {
      var obj = Object.create({
        targetFn: targetFn
      });
      var promisifiedObj = promisify.promisifyObject(obj);

      promisifiedObj
        .targetFn(12)
        .then(function(result) {
          expect(result).to.eql(12);

          done();
        });

      $rootScope.$digest();
    });

    it('should not promisify methods from prototype when onlyOwnProperties is true', function() {
      var obj = Object.create({
        targetFn: targetFn
      });
      var promisifiedObj = promisify.promisifyObject(obj, null, true);

      expect(promisifiedObj.targetFn).to.equal(targetFn);
    });

    it('should promisify only chosen methods', function(done) {
      var obj = promisify.promisifyObject({
        targetFn: targetFn,
        fn2: function() {
          return 2;
        }
      }, ['targetFn']);

      obj
        .targetFn(12)
        .then(function(result) {
          expect(result).to.eql(12);

          done();
        });

      expect(obj.fn2()).to.eql(2);

      $rootScope.$digest();
    });

    it('should run original method with correct this context', function(done) {
      var obj = {
        method: function(cb) {
          cb(null, this);
        }
      };
      var promisifiedObj = promisify.promisifyObject(obj);

      promisifiedObj
        .method()
        .then(function(usedThis) {
          expect(usedThis).to.eql(obj);

          done();
        });

      $rootScope.$digest();
    });
  });

  describe('promisifyFunction', function() {
    var promisifiedFn;

    beforeEach(function() {
      promisifiedFn = promisify.promisifyFunction(targetFn);
    });

    it('should promisify node callback function', function(done) {
      promisifiedFn(12)
        .then(function(result) {
          expect(result).to.eql(12);

          done();
        });

      $rootScope.$digest();
    });

    it('should return failed promise', function(done) {
      promisifiedFn(5)
        .catch(function(ex) {
          expect(ex.error).to.eql(5);

          done();
        });

      $rootScope.$digest();
    });
  });

  function targetFn(x, callback) {
    if (x > 10) {
      callback(null, x);
    } else {
      callback({error: x});
    }
  }
});
