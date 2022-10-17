const core = require('@actions/core');
const github = require('@actions/github');


module.exports = async function() {
  try {
    const issueNumber = core.getInput('issue');
    const deepCopy = core.getInput('deep-copy');
    const repoToken = core.getInput('repo-token');
    const newVersion = core.getInput('version');
    const versionPrefix = core.getInput('version-prefix');
    
    const octokit = github.getOctokit(repoToken);
    const repo = github.context.payload.repository;
    
    const { data: issue } = await octokit.rest.issues.get({
      owner: repo.owner.login,
      repo: repo.name,
      issue_number: issueNumber,
    });
    
    const issueBody = issue.body;
    var newIssueBody = issue.body;
    
    const newVersionLabel = {name: `${versionPrefix}${newVersion}`};
    const createNewIssueLabels = function (previousLabels) {
      const newLabels = previousLabels.filter(label => !label.name.startsWith(versionPrefix));
      newLabels.push(newVersionLabel);
      return newLabels;
    }
    
    if (deepCopy && issue.body) {
      // match #<digits> and only when there is space/new line characters around it
      const issueRegex = /(\s+#)(\d+)(\s+)/g;
      
      const matches = issueBody.matchAll(issueRegex);
      
      newIssueBody = '';
      var previousMatchOffset = 0;
      
      for (const match of matches) {
        const { data: referencedIssue } = await octokit.rest.issues.get({
          owner: repo.owner.login,
          repo: repo.name,
          issue_number: match[2],
        });
        
        const newReferencedIssueLabels = createNewIssueLabels(referencedIssue.labels);
        
        const { data: newReferencedIssue } = await octokit.rest.issues.create({
          owner: repo.owner.login,
          repo: repo.name,
          title: referencedIssue.title,
          body: referencedIssue.body,
          labels: newReferencedIssueLabels
        });
        
        core.info(`Copied referenced issue ${referencedIssue.number} into new issue ${newReferencedIssue.number}.`);
        
        newIssueBody = newIssueBody + issueBody.substring(previousMatchOffset, match.index)
          + match[1] + newReferencedIssue.number + match[3];
          
        previousMatchOffset = match.index + match[0].length;
          
      }
      newIssueBody = newIssueBody + issueBody.substring(previousMatchOffset);
    }
    
    const newIssueLabels = createNewIssueLabels(issue.labels);
    
    const { data: newIssue } = await octokit.rest.issues.create({
      owner: repo.owner.login,
      repo: repo.name,
      title: issue.title,
      body: newIssueBody,
      labels: newIssueLabels
    });
    
    core.info(`Copied issue ${issueNumber} into new issue ${newIssue.number}.`);
  } catch (error) {
    core.setFailed(error.message);
  }
}