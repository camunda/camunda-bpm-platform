'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./repository-setup');

var repositoryPage = require('../pages/repository');
var deploymentsPage = repositoryPage.deployments;
var resourcesPage = repositoryPage.resources;
var resourcePage = repositoryPage.resource;

describe('Repository Spec', function() {

  describe('deployments sorting', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });

    // handled by dashboard
    xit('shows an active link in the header', function () {
      expect(repositoryPage.navbarItem(1).getText()).to.eventually.eql('Deployments');
      expect(repositoryPage.navbarItem(1).getAttribute('class')).to.eventually.contain('active');
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

    it('should change sorting order on URL change', function() {
      browser.getLocationAbsUrl().then(function(url) {

        expect(url).to.contain('deploymentsSortBy=name');

        var location = url.substr(url.indexOf('#') + 2);

        location = location.replace('deploymentsSortBy=name', 'deploymentsSortBy=id');

        browser.setLocation(location).then(function() {
          expect(deploymentsPage.sortingBy()).to.eventually.eql('ID');
        });

      });
    });

    it('should preserve sorting order on unrelated URL change', function() {
      browser.setLocation('repository').then(function() {
        expect(deploymentsPage.sortingBy()).to.eventually.eql('ID');
      });
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
        deploymentsPage.createSearch('Name', 'xyz');

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
        deploymentsPage.createSearch('Time', 'before', '2015-01-01T00:00:00', true);

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

    describe('search by deployment source', function() {

      it('should search by invalid deployment source', function() {

        // when
        deploymentsPage.createSearch('Source', 'xyz');

        //then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(0);
      });

      it('should search by process application deployment source', function() {

        // when
        deploymentsPage.changeValue(0, 'process application');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('first-deployment');
        expect(deploymentsPage.deploymentSource(0)).to.eventually.eql('process application');
      });

      it('should search for undefined deployment sources', function() {

        // when
        deploymentsPage.changeType(0, 'Source Undefined');

        // then
        expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
        expect(deploymentsPage.deploymentName(0)).to.eventually.eql('second-deployment');
        expect(deploymentsPage.deploymentSource(0)).to.eventually.eql('null');
      });

    });

  });

  describe('deployment selection', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should initially select first deployment', function() {
      // then
      expect(deploymentsPage.isDeploymentSelected(0)).to.eventually.be.true;
    });


    it('should select a deployment', function() {
      // given
      expect(deploymentsPage.isDeploymentSelected(0)).to.eventually.be.true;
      expect(deploymentsPage.isDeploymentSelected(1)).to.eventually.be.false;
      expect(deploymentsPage.isDeploymentSelected(2)).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment(2);

      // then
      expect(deploymentsPage.isDeploymentSelected(0)).to.eventually.be.false;
      expect(deploymentsPage.isDeploymentSelected(1)).to.eventually.be.false;
      expect(deploymentsPage.isDeploymentSelected(2)).to.eventually.be.true;
    });

  });

  describe('delete deployment', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should open delete deployment modal', function() {
      // given
      deploymentsPage.deploymentName(2).then(function(name) {

        // when
        deploymentsPage.selectDeployment(2);
        deploymentsPage.openDeleteDeployment(2);

        // then
        expect(deploymentsPage.modalTitle()).to.eventually.eql('Delete Deployment: ' + name);

        expect(deploymentsPage.infobox().isPresent()).to.eventually.be.true;

        expect(deploymentsPage.cascadeCheckbox().isSelected()).to.eventually.be.false;
        expect(deploymentsPage.skipCustomListenersCheckbox().isSelected()).to.eventually.be.true;

        expect(deploymentsPage.deleteButton().isPresent()).to.eventually.be.true;
        expect(deploymentsPage.deleteButton().isEnabled()).to.eventually.be.false;

        expect(deploymentsPage.closeButton().isPresent()).to.eventually.be.true;
      });

    });


    it('should close delete deployment modal', function() {

      // when
      deploymentsPage.closeModal();

      // then
      expect(deploymentsPage.modalContent().isPresent()).to.eventually.be.false;
    });


    it('should enable delete button', function() {
      // given
      deploymentsPage.selectDeployment(2);
      deploymentsPage.openDeleteDeployment(2);

      expect(deploymentsPage.cascadeCheckbox().isSelected()).to.eventually.be.false;

      expect(deploymentsPage.deleteButton().isPresent()).to.eventually.be.true;
      expect(deploymentsPage.deleteButton().isEnabled()).to.eventually.be.false;


      // when
      deploymentsPage.cascadeCheckbox().click();

      // then
      expect(deploymentsPage.cascadeCheckbox().isSelected()).to.eventually.be.true;
      expect(deploymentsPage.deleteButton().isEnabled()).to.eventually.be.true;
    });


    it('should delete deployment', function() {

      // when
      deploymentsPage.deleteDeployment();

      // then
      expect(deploymentsPage.deploymentList().count()).to.eventually.eql(2);
    });


    it('should initially select first deployment', function() {
      // then
      expect(deploymentsPage.isDeploymentSelected(0)).to.eventually.be.true;
    });

  });

  describe('resource details view', function () {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should display info text when no resource is selected', function() {

      // then
      expect(resourcePage.noResourceInfoText()).to.eventually.eql('Select a resource in the list.');
    });


    it('should appear when a resource is selected', function() {

      // when
      resourcesPage.resourceName(0).then(function(name) {

        resourcesPage.selectResource(0);

        // then
        expect(resourcePage.resourceName()).to.eventually.eql(name);

      });

    });


    it('should display version of the resource', function() {
      expect(resourcePage.resourceVersion()).to.eventually.eql('Version: 1');
    });
  });

  describe('diagram', function() {

    before(function() {
      return testHelper(setupFile.setup2, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should display bpmn diagram', function() {
      // given
      expect(resourcePage.bpmnDiagramFormElement().isPresent()).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment('bpmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.bpmnDiagramFormElement().isPresent()).to.eventually.be.true;

    });


    it('should display cmmn diagram', function() {
      // given
      expect(resourcePage.cmmnDiagramFormElement().isPresent()).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment('cmmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.cmmnDiagramFormElement().isPresent()).to.eventually.be.true;

    });


    it('should display dmn diagram', function() {
      // given
      expect(resourcePage.dmnDiagramFormElement().isPresent()).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment('dmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.dmnDiagramFormElement().isPresent()).to.eventually.be.true;

    });


    it('should display image', function() {
      // given
      expect(resourcePage.imageFormElement().isPresent()).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment('image');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.imageFormElement().isPresent()).to.eventually.be.true;

    });


    it('should display script', function() {
      // given
      expect(resourcePage.unkownResourceFormElement().isPresent()).to.eventually.be.false;

      // when
      deploymentsPage.selectDeployment('script');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.unkownResourceFormElement().isPresent()).to.eventually.be.true;

    });

  });

  describe('download button', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should not display download button', function() {

      // then
      expect(resourcePage.downloadButton().isPresent()).to.eventually.be.false;
    });


    it('should display download button', function() {
      // whe
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.downloadButton().isPresent()).to.eventually.be.true;
    });

  });

  describe('definitions tab', function() {

    before(function() {
      return testHelper(setupFile.setup2, function() {
        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });


    it('should display deployed process definitions', function() {

      // when
      deploymentsPage.selectDeployment('bpmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.definitions.table().count()).to.eventually.eql(1);

      expect(resourcePage.definitions.name(0).getText()).to.eventually.eql('User Tasks');
      expect(resourcePage.definitions.key(0).getText()).to.eventually.eql('user-tasks');
      expect(resourcePage.definitions.instanceCount(0).getText()).to.eventually.eql('2');
    });


    it('should be empty', function() {

      // when
      deploymentsPage.selectDeployment('script');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.definitions.table().count()).to.eventually.eql(0);
    });

    it('should display deployed case definitions', function() {

      // when
      deploymentsPage.selectDeployment('cmmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.definitions.table().count()).to.eventually.eql(1);

      expect(resourcePage.definitions.name(0).getText()).to.eventually.eql('Loan Application');
      expect(resourcePage.definitions.key(0).getText()).to.eventually.eql('loanApplicationCase');
      expect(resourcePage.definitions.instanceCount(0).getText()).to.eventually.eql('0');
    });

    it('should display deployed decision definitions', function() {

      // when
      deploymentsPage.selectDeployment('dmn');
      resourcesPage.selectResource(0);

      // then
      expect(resourcePage.definitions.table().count()).to.eventually.eql(1);

      expect(resourcePage.definitions.name(0).getText()).to.eventually.eql('Assign Approver');
      expect(resourcePage.definitions.key(0).getText()).to.eventually.eql('invoice-assign-approver');
    });

    it('should not display the version for dmn resources in the header', function() {
      expect(resourcePage.resourceVersionElement().isDisplayed()).to.eventually.be.false;
    });

    it('should contain the version in the definitions table', function() {
      expect(resourcePage.definitions.version(0).getText()).to.eventually.eql('1');
    });

    it('should display deployed decision requirements definitions', function() {
      resourcesPage.selectResource(1);

      // then
      expect(resourcePage.definitions.table().count()).to.eventually.eql(2);

      expect(resourcePage.drdTable.name().getText()).to.eventually.eql('camunda');
      expect(resourcePage.drdTable.key().getText()).to.eventually.eql('definitions');
      expect(resourcePage.drdTable.version().getText()).to.eventually.eql('1');
    });

  });

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        repositoryPage.navigateToWebapp('Cockpit');
        repositoryPage.authentication.userLogin('admin', 'admin');
        repositoryPage.navigateTo();
      });
    });

    it('should search by invalid tenant id', function() {

      // when
      deploymentsPage.createSearch('Tenant ID', 'nonExisting');

      //then
      expect(deploymentsPage.deploymentList().count()).to.eventually.eql(0);
    });

    it('should search by tenant id', function() {

      // when
      deploymentsPage.changeValue(0, 'tenant1');

      // then
      expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
      expect(deploymentsPage.deploymentTenantId(0)).to.eventually.eql('tenant1');
    });

    it('should search for deployment without tenant id', function() {

      // when
      // prevent tooltip to intercept click
      browser.actions().
          mouseDown(element(by.css('body'))).
          perform();
      deploymentsPage.changeType(0, 'without Tenant ID');

      // then
      expect(deploymentsPage.deploymentList().count()).to.eventually.eql(1);
      expect(deploymentsPage.deploymentTenantId(0)).to.eventually.eql('null');
    });

  });

});
