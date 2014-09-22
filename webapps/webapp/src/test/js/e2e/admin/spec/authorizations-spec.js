/*
 * */
'use strict';

var authorizationsPage = require('../pages/authorizations');

describe('authorizations page -', function() {

  describe('start test', function () {

    it('should login', function () {

      console.log('\n' + 'authorizations-spec');

      // when
      authorizationsPage.navigateToWebapp('Admin');
      authorizationsPage.authentication.userLogin('jonny1', 'jonny1');
      authorizationsPage.selectNavbarItem('Authorizations');

      // then
      authorizationsPage.application.isActive();
      authorizationsPage.application.loggedInUser('demo');
      expect(authorizationsPage.pageHeader()).toBe('Authorizations');
    });

  });


  describe('authorizations sub pages', function() {

    beforeEach(function() {

      authorizationsPage.navigateToWebapp('Admin');
      authorizationsPage.selectNavbarItem('Authorizations');
    });


    it('should validate application page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Application');

      // then
      authorizationsPage.application.isActive();
      expect(authorizationsPage.application.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.application.boxHeader()).toBe('Application Authorizations');
    });


    it('should validate authorization page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Authorization');

      // then
      authorizationsPage.authorization.isActive();
      expect(authorizationsPage.authorization.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.authorization.boxHeader()).toBe('Authorization Authorizations');
    });


    it('should validate filter page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Filter');

      // then
      authorizationsPage.filter.isActive();
      expect(authorizationsPage.filter.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.filter.boxHeader()).toBe('Filter Authorizations');
    });


    it('should validate group page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group');

      // then
      authorizationsPage.group.isActive();
      expect(authorizationsPage.group.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.group.boxHeader()).toBe('Group Authorizations');
    });


    it('should validate group membership page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group Membership');

      // then
      authorizationsPage.groupMembership.isActive();
      expect(authorizationsPage.groupMembership.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.groupMembership.boxHeader()).toBe('Group Membership Authorizations');
    });


    it('should validate user page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('User');

      // then
      authorizationsPage.user.isActive();
      expect(authorizationsPage.user.createNewButton().isEnabled()).toBe(true);
      expect(authorizationsPage.user.boxHeader()).toBe('User Authorizations');
    });

  });


  describe('end test', function() {

    it('should logout', function() {

      authorizationsPage.logout();
    });

  });

});