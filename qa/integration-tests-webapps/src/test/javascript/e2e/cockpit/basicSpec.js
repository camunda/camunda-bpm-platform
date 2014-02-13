describe('cockpit dashboard', function() {

  function login(username, password, valid) {    
    element(by.model('username')).clear();
    element(by.model('password')).clear();
    element(by.model('username')).sendKeys(username);
    element(by.model('password')).sendKeys(password);

    var submitButton = element(by.css('.btn-primary.btn-large'));
    submitButton.click();
    if ((valid == true) || (valid == undefined) ) {
        expectLoginSuccessful(username);
    } else {
        expectLoginFailed();
    }
  }

  function expectLoginSuccessful(username) {
    var loggedInUserMenu = element(by.binding('authentication.user.name'));
    expect(loggedInUserMenu.getText()).toEqual(username);
  }

  function expectLoginFailed() {
    // TODO: check for ERROR-Notification
    // var notification = element(by.css('.notifications [sem-show-login-information]'));
    // expect(notification.getText()).toEqual("Wrong credentials or missing access rights to application");
  }

  function checkPluginHeaderName_h1(headerName) {
    var header = element(by.tagName('h1'));
    expect(header.getText()).toEqual(headerName);
  }


  function openTasklist() {
    element(by.css('.icon-home.icon-white')).click();
    element(by.css('.dropdown-menu ul li:nth-child(1)')).click();  
    //TODO select Tasklist in dropdown
  }

  function openAdmin() {
    element(by.css('.icon-home.icon-white')).click();
    //TODO select Admin in dropdown
  }

  function openCockpit() {
    element(by.css('.icon-home.icon-white')).click();
    //TODO select Cockpit in dropdown
  }

  function selectProcessInList(processName) {   
    var processes = element.all(by.repeater('statistic in statistics').column('{{ statistic.definition.name }}'));
    
    processes.then(function(arr) {
      for (var i in arr) {
        arr[i].getText().then(function(text) {
          if (text == processName) {
                arr[i].click();                
          };
        });
      };   
    });      
  }

  it('should validate credentials', function() {
    browser.driver.manage().window().maximize();
    browser.get('http://localhost:8080/camunda');
    login('jonny1', 'jonny3', false);
    login('jonny1', 'jonny1', true);
  });

  it('should check existenc of plugin', function() {
    checkPluginHeaderName_h1('Deployed Processes (List)');
  });

  it('should find process in Deployed Processes (List)', function() {
    var items = element(by.repeater('statistic in statistics').row(0).column('definition.name'));

    expect(items.getText()).toEqual('Another Failing Process');
  });    

  it('it should select process in Deployed Process (List)', function() {
    var items = element(by.repeater('statistic in statistics').row(3).column('definition.name'));
    
    expect(items.getText()).toEqual('Cornercases Process');
    items.click();

    var processDefintionName = element(by.binding('processDefinition.name'));
    expect(processDefintionName.getText()).toEqual('Cornercases Process');
  });

  it('it should select process instance in Process Instances Table', function() {
    var items = element(by.repeater('processInstance in processInstances').row(1).column('id'));
    items.click();

  });  

  it('should select activity in diagram', function() {
    element(by.css('.process-diagram *[data-activity-id="UserTask_2"]')).click();

    //verify that element is highlighted. class="bpmnElement activity-highlight"
    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]')).getAttribute('class')).toMatch('activity-highlight');
  });

  it('should switch tab in instance details view', function() {
    element(by.repeater('tabProvider in processInstanceTabs').row(3).column('label')).click();

    var tabContent = element(by.css('view[provider=selectedTab]')).findElement(by.repeater('userTask in userTasks').row(0).column('name'));    
    expect(tabContent.getText()).toEqual('Inner Task');
  });

  it('should deselect activity in diagram', function() {
    var ptor = protractor.getInstance();
    var userTask = element(by.css('.process-diagram *[data-activity-id="UserTask_2"]'));

    //TODO deselct UserTask_2 by CTRL + Click() 

    //ptor.actions().sendKeys(protractor.Key.CONTROL).perform();

    // ptor.actions().keyDown(protractor.Key.CONTROL);
    //   .click(userTask)
    //   .keyUp(protractor.Key.CONTROL)
    //   .perform()

    //verify that element is not highlighted. class="bpmnElement activity-highlight"
    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]')).getAttribute('class')).not.toMatch('activity-highlight');
  });

/*  it('should select Activity in instance details view and highlight element in renderer', function() {

    element(by.css('view[provider=selectedTab]')).findElement(by.repeater('userTask in userTasks').row(0).column('name')).click();


    element(by.css('view[provider=selectedTab]')).findElement(by.repeater('userTask in userTasks').row(1).column('name')).click();

    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_3"]')).getAttribute('class')).toMatch('activity-highlight');
  });  */

});


// FIND PROCESS IN LIST
//-------------------------------------------------------------------------------------------------------------------
// asynchronous call 

/*    var processes = element.all(by.repeater('statistic in statistics').column('{{ statistic.definition.name }}'));
    var proc;

    runs(function() {
      processes.then(function(arr) {
        for (var i in arr) {
          arr[i].getText().then(function(text) {
            console.log(arr[i].getText());
            if (text === 'form examples') {
              proc = arr[i];
            }
          });
        };
      });
    });

    waitsFor(function() {
      return !!proc;
    });
    
    runs(function() {
      proc.click();

      var processDefintionName = element(by.binding('processDefinition.name'));      
      expect(processDefintionName.getText()).toEqual('form examples');
    });*/
//-------------------------------------------------------------------------------------------------------------------
