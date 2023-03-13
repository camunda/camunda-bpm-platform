/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var expect = require('chai').expect;

describe('The AbstractClientResource', function() {
  var AbstractClientResource, Extended1, Extended2, instance1, instance2;

  it('does not blow when loading', function() {
    expect(function() {
      AbstractClientResource = require('./../../lib/api-client/abstract-client-resource');
    }).not.to.throw();
  });


  it('can be extend', function() {
    expect(function() {
      Extended1 = AbstractClientResource.extend({
        instanceMethod: function() {},
        instanceProperty: true
      }, {
        staticMethod: function() {},
        staticProperty: true
      });

      Extended2 = AbstractClientResource.extend({
        otherInstanceMethod: function() {},
        otherInstanceProperty: true
      }, {
        otherStaticMethod: function() {},
        otherStaticProperty: true
      });
    }).not.to.throw();
  });


  describe('generated resource class', function() {
    it('has a `static` properties', function() {
      expect(typeof Extended1.staticMethod).to.eql('function');
      expect(typeof Extended1.staticProperty).to.eql('boolean');
      expect(typeof Extended1.path).to.not.be.undefined;

      expect(typeof Extended2.staticMethod).to.eql('undefined');
      expect(typeof Extended2.staticProperty).to.eql('undefined');
      expect(typeof Extended2.path).to.not.be.undefined;

      expect(typeof Extended2.otherStaticMethod).to.eql('function');
      expect(typeof Extended2.otherStaticProperty).to.eql('boolean');
    });


    it('instanciates', function() {
      expect(function() {
        instance1 = new Extended1();

        instance2 = new Extended2();
      }).not.to.throw();
    });


    it('has a `instance` properties', function() {
      expect(typeof instance1.instanceMethod).to.eql('function');
      expect(typeof instance1.instanceProperty).to.eql('boolean');

      expect(typeof instance2.instanceMethod).to.eql('undefined');
      expect(typeof instance2.instanceProperty).to.eql('undefined');

      expect(typeof instance2.otherInstanceMethod).to.eql('function');
      expect(typeof instance2.otherInstanceProperty).to.eql('boolean');
    });
  });
});
