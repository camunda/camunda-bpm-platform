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

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common createListQueryFunction', function() {
  var $rootScope;
  var $q;
  var getCount;
  var getList;
  var query;

  beforeEach(module(camCommon.name));

  beforeEach(inject(function(_$rootScope_, _$q_, createListQueryFunction) {
    $rootScope = _$rootScope_;
    $q = _$q_;

    getCount = sinon.stub().returns(
      $q.when({
        count: 1
      })
    );
    getList = sinon.stub();
    query = createListQueryFunction(getCount, getList);
  }));

  it('should call getCount function to fetch total count of elements', function() {
    var params = {
      a: 1
    };

    query(params);

    expect(getCount.calledWith(params)).to.eql(true);
  });

  it('should return only count when first result position is bigger than count', function(done) {
    var pages = {
      current: 10,
      size: 100
    };
    getCount.returns(
      $q.when({
        count: 200
      })
    );

    query({}, pages).then(function(data) {
      expect(data).to.contain.keys('count');
      expect(data).not.to.contain.keys('list');
      expect(getList.called).to.eql(
        false,
        'expected getList to never have been called'
      );

      done();
    });

    $rootScope.$digest();
  });

  it('should return count and list when first result position is smaller than count', function(done) {
    var pages = {
      current: 10,
      size: 100
    };
    var params = {
      a: 1
    };
    getCount.returns(
      $q.when({
        count: 20000000
      })
    );

    query(params, pages).then(function(data) {
      expect(data).to.contain.keys('count');
      expect(data).to.contain.keys('list');
      expect(
        getList.calledWith({
          a: params.a,
          firstResult: 900,
          maxResults: pages.size
        })
      ).to.eql(
        true,
        'expected getList to be called with params and pagination'
      );

      done();
    });

    $rootScope.$digest();
  });
});
