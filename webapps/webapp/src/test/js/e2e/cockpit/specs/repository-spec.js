'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./repository-setup');

var repositoryPage = require('../pages/repository');
var deploymentsPage = repositoryPage.deployments;

describe('Repository Spec', function() {

  describe('deployments sorting', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should change sorting by', function() {
      // given
      expect(deploymentsPage.sortingBy()).to.eventually.eql('Deployment Time');

      // when
      deploymentsPage.changeSortingBy('Name');

      // then
      expect(deploymentsPage.sortingBy()).to.eventually.eql('Name');
      expect(deploymentsPage.deploymentName(0)).to.eventually.eql('third-deployment');
    });


    it('should change sorting order', function() {
      // given
      expect(deploymentsPage.isSortingDescending()).to.eventually.be.true;

      // when
      deploymentsPage.changeSortingDirection();

      // then
      expect(deploymentsPage.isSortingDescending()).to.eventually.be.false;
      expect(deploymentsPage.isSortingAscending()).to.eventually.be.true;
      expect(deploymentsPage.deploymentName(0)).to.eventually.eql('first-deployment');
    });

  });

  describe('deployments search', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });

    describe('add a search pill', function() {

      it('should use wrong deployment name and find nothing', function() {

        // when
        deploymentsPage.createSearch('Name', '=', 'xyz');

        //then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(0);
      });


      it('should change deployment name and find one deployment', function() {

        // when
        deploymentsPage.changeValue(0, 'second-deployment');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('second-deployment');
      });


      it('should change operator and find one deployment', function() {

        // when
        deploymentsPage.changeOperator(0, 'like');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('second-deployment');
      });


      it('should change value and find all deployments', function() {

        // when
        deploymentsPage.changeValue(0, '-deployment');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(3);
      });

    });


    describe('add more search pills', function() {


      it('should add Date search and find nothing', function() {

        // when
        deploymentsPage.createSearch('Deployment Time', 'before', '2015-01-01T00:00:00', true);

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(0);
      });


      it('should change operator and find all deployments', function() {

        // when
        deploymentsPage.changeOperator(1, 'after');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(3);
      });


      it('should keep search pills after page refresh', function() {

        // given
        deploymentsPage.changeOperator(0, '=');
        deploymentsPage.changeValue(0, 'first-deployment');

        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('first-deployment');

        // when
        browser.getCurrentUrl().then(function(url) {
          browser.get(url).then(function() {
            browser.sleep(500);
          });
        });

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('first-deployment');
      });
    });


    describe('remove search pill', function() {

      it('should remove Name search', function() {

        // when
        deploymentsPage.deleteSearch(0);

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(3);
      });

      it('should remove Deployment Time search', function() {

        // when
        deploymentsPage.deleteSearch(0);

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(3);
      });

    });

  });

});
