# How to contribute

We would love you to contribute to this project. You can do so in various ways.


## Contribute your knowledge

Help others by participating in our [forum](https://forum.camunda.org/).

Before you ask a question, search the forum. The answer may have already been provided.


## File bugs or feature requests

[File bugs](https://app.camunda.com/jira/) you found in the code or [features](https://app.camunda.com/jira/) you would like to see in the future in our [issue tracker](https://app.camunda.com/jira/).

* Register yourself on our [issue tracker](https://app.camunda.com/jira/)
* Search the list of open issues first, your bug or feature may have already been reported
* When filing a bug
  * Be concise
  * Qualify steps to reproduce a problem
  * Specify environment you found the bug in (library version etc.)
  * If possible, attach a test case that reproduces the problem


## Contribute code

You can contribute code that fixes bugs and/or implements features.

* [Start with an EasyPick](https://app.camunda.com/jira/issues/?jql=project%20%3D%20CAM%20AND%20labels%20%3D%20EasyPick%20AND%20status%20%3D%20Open): In our Jira we label issues which we consider small and easy to start on as EasyPicks.
* Have a look on [what we are working on](https://app.camunda.com/jira/secure/RapidBoard.jspa?rapidView=39) first to check that we do not perform duplicated work
* Read through our contribution guide lines
* Implement the feature or bug fix on a [feature branch and submit the result as a pull request](http://lefedt.de/blog/posts/2013/contributing-to-oss-through-pull-requests/)


## Write Tests

Best practices for writing test cases:

* write JUnit4-style tests, not JUnit3
* Project `camunda-engine`: If you need a process engine object, use the JUnit rule `org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule`. It ensures that the process engine object is reused across test cases and that certain integrity checks are performed after every test. For example:
  ```
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void testThings() {
    ProcessEngine engine = engineRule.getProcessEngine();

    ...
  }
  ```
* Project `camunda-engine`: If you need a process engine with custom configuration, use the JUnit rule `org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule` and chain it with `org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule` like so:
  ```
  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      // apply configuration options here

      return configuration;
    }
  };
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);
  ```


## Coding styles

* tabs as spaces / tab size 2 (all files)
* If you created a number of commits, [squash](http://gitready.com/advanced/2009/02/10/squashing-commits-with-rebase.html) your work into a few commits only.
* Create commit messages that adhere to our commit message style(see below).


## Commit Message Conventions

> This page defines a convention for commit messages for [camundaBPM](http://camunda.org) related projects.
>
> All commits pushed to the [camunda BPM](https://github.com/camunda) repositories must conform to that convention.

The contents of this page are partly based on the [angular commit messages document](https://docs.google.com/document/d/1QrDFcIiPjSLDn3EL15IJygNPiHORgU1_OOAqWjiDU5Y/edit?pli=1).


### Purpose

The commit message is what is what describes your contribution.
Its purpose must therefore be to document what a commit contributes to a project.

Its head line __should__ be as meaningful as possible because it is always
seen along with other commit messages.

Its body __should__ provide information to comprehend the commit for people
who care.

Its footer __may__ contain references to external artifacts
(issues it solves, related commits) as well as breaking change notes.

This applies to __all kind of projects__.


### Format

#### Short form (only subject line)

    <type>(<scope>): <subject>

#### Long form (with body)

    <type>(<scope>): <subject>

    <BLANK LINE>

    <body>

    <BLANK LINE>

    <footer>

First line cannot be longer than __70 characters__, second line is always blank and other lines should be wrapped at __80 characters__! This makes the message easier to read on github as well as in various git tools.

### Subject Line

The subject line contains succinct description of the change.

#### Allowed <type>

 * feat (feature)
 * fix (bug fix)
 * docs (documentation)
 * style (formatting, missing semi colons, …)
 * refactor
 * test (when adding missing tests)
 * chore (maintain)
 * improve (improvement, e.g. enhanced feature)

#### Allowed <scope>

Scope could be anything specifying place of the commit change. For example in the [camunda-modeler project](https://github.com/camunda/camunda-modeler) this could be import, export, property panel, label, id, ...

#### <subject> text

 * use imperative, present tense: _change_ not _changed_ nor _changes_ or _changing_
 * do not capitalize first letter
 * do not append dot (.) at the end

### Message Body

 * just as in <subject> use imperative, present tense: _change_ not _changed_ nor _changes_ or _changing_
 * include motivation for the change and contrast it with previous behavior

### Message Footer

#### Breaking changes

All breaking changes have to be mentioned in footer with the description of the change, justification and migration notes

    BREAKING CHANGE: Id editing feature temporarily removed

        As a work around, change the id in XML using replace all or friends

#### Referencing issues

Closed bugs / feature requests / issues should be listed on a separate line in the footer prefixed with "Closes" keyword like this:

    Closes #234

or in case of multiple issues:

    Closes #123, #245, #992

### More on good commit Messages

 * http://365git.tumblr.com/post/3308646748/writing-git-commit-messages
 * http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html

### FAQ for Geeks

##### Why to use imperative form in commit messages?
I.e. why to use _add test for #foo_ versus _added test for #foo_ or _adding test for foo_?

Makes commit logs way more readable. See the work you did during a commit as a work on an issue and the commit as solving that issue. Now write about for what issue the commit is a result rather than what you did or are currently doing.

__Example:__ You write a test for the function #foo. You commit the test. You use the commit message _add test for #foo_. Why? Because that is what the commit solves.

##### How to categorize commits which are direct follow ups to merges?
Use `chore(merge): <what>`.

##### I want to commit a micro change. What should I do?
Ask yourself, why it is only a micro change. Use feat = _docs_, _style_ or _chore_ depending on the change of your merge. Please see next question if you consider commiting work in progress.

##### I want to commit work in progress. What should I do?
Do not do it or do it (except for locally) or do it on a non public branch (ie. non master / develop ...) if you need to share the stuff you do.

When you finished your work, [squash](http://gitready.com/advanced/2009/02/10/squashing-commits-with-rebase.html) the changes to commits with reasonable commit messages and push them on a public branch.
