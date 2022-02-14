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

describe('The Tenant resource usage', function() {
  var Tenant;

  it('does not blow when loading', function() {
    expect(function() {
      Tenant = require('./../../../lib/api-client/resources/tenant');
    }).not.to.throw();
  });

  it('has a `path` static property', function() {
    expect(Tenant.path).to.eql('tenant');
  });

  it('has a `create` method', function() {
    expect(Tenant.create).to.be.a('function');
  });

  it('has a `count` method', function() {
    expect(Tenant.count).to.be.a('function');
  });

  it('has a `get` method', function() {
    expect(Tenant.get).to.be.a('function');
  });

  it('has a `list` method', function() {
    expect(Tenant.list).to.be.a('function');
  });

  it('has a `createUserMember` method', function() {
    expect(Tenant.createUserMember).to.be.a('function');
  });

  it('has a `createGroupMember` method', function() {
    expect(Tenant.createGroupMember).to.be.a('function');
  });

  it('has a `deleteUserMember` method', function() {
    expect(Tenant.deleteUserMember).to.be.a('function');
  });

  it('has a `deleteGroupMember` method', function() {
    expect(Tenant.deleteGroupMember).to.be.a('function');
  });

  it('has a `update` method', function() {
    expect(Tenant.update).to.be.a('function');
  });

  it('has a `delete` method', function() {
    expect(Tenant.delete).to.be.a('function');
  });

  it('has a `options` method', function() {
    expect(Tenant.options).to.be.a('function');
  });
});
