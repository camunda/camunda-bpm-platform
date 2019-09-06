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

var chai = require('chai'),
    directivesModule = require('../index');

var expect = chai.expect;

describe('camunda-commons-ui/directives/autoFill', function() {

  beforeEach(window.module(directivesModule.name));


  describe('bootstrap', function() {

    it('should bootstrap with module', inject(function($compile, $rootScope, $interval) {

      var element = $compile('<input ng-model="foo" auto-fill>')($rootScope);
      $rootScope.$digest();

      element.val('FOO');

      $interval.flush(500);
      $rootScope.$digest();

      expect($rootScope.foo).to.equal('FOO');
    }));

  });

});
