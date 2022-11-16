const core = require('@actions/core');
const github = require('@actions/github');
const jsonUtils = require('./utils/json-utils.js');
const collectionUtils = require('./utils/collection-utils.js');

module.exports = async function () {
  try {
    const versions = core.getInput('versions');
    const versionPrefix = core.getInput('version-prefix');
    const repoToken = core.getInput('repo-token');
    
    var queryLabels = versions.split(",");
    queryLabels.forEach(function(part, index) {
      this[index] = versionPrefix + part.trim();
    }, queryLabels);
    
    console.log(`Extracting HTTP(S) links from all issues matching labels: ${queryLabels}!`);
    
    const octokit = github.getOctokit(repoToken)
    
    const repo = github.context.payload.repository;
    
    // regex is supposed to match JIRA-style links 
    // (i.e. ensures that there is a sequence LLLL-DDD where L is a letter and D is a digit
    const linkRegex = /\bhttps?:\/\/\S+[A-Za-z]+-[0-9]+/gi;
    
    var links = new Map();    // issue id => [url, ..]
    const issues = new Map();   // issue id => {}
    
    // Discovered by trial and error, it seems that the octokit client does
    // not support querying for issues that match one of a set of labels (neither issues.listForRepo
    // nor search.issuesAndPullRequests work). That's why we process one label after the other.
    for (const label of queryLabels) {
      core.debug(`Querying for label ${label}`);
      
      await octokit.paginate(octokit.rest.issues.listForRepo, {
        owner: repo.owner.login,
        repo: repo.name,
        labels: label,
        state: 'all'
      })
      .then(issuesforLabel => {
        
        core.debug(`Issues returned for label ${label}: ${issuesforLabel}`);
        for (const issue of issuesforLabel) {
          issues.set(issue.id, { url: issue.html_url, owner: repo.owner.login, repo: repo.name, number: issue.number });
          
          const issueUrl = issue.html_url;
          core.debug(`Processing issue ${issueUrl}`);
          if (issue.body != null && !links.has(issueUrl)) {
            const linkMatches = issue.body.matchAll(linkRegex);
            const linksForIssue = [];
            
            for (const [url] of linkMatches) {
              linksForIssue.push(url);
            }
            
            if (linksForIssue.length > 0) {
              links.set(issue.id, linksForIssue);
            }
            
            core.debug(`In issue ${issueUrl}, found the following links: ${linksForIssue}`);
          }
        }
      })
    }
       
    links = collectionUtils.sortMapByIntKeys(links);
    
    var linksToIssues = collectionUtils.revertMultiMap(links); // url => [issue id, ..]
    linksToIssues = collectionUtils.sortMapByIntKeys(linksToIssues);
        
    core.debug(`Result links: ${jsonUtils.stringifyMapToJSON(links)}`);
    core.debug(`Result reverse links: ${jsonUtils.stringifyMapToJSON(linksToIssues)}`);
    core.debug(`Issues: ${jsonUtils.stringifyMapToJSON(issues)}`);
    
    core.setOutput('links', jsonUtils.stringifyMapToJSON(links));
    core.setOutput('links-reverse', jsonUtils.stringifyMapToJSON(linksToIssues));
    core.setOutput('issues', jsonUtils.stringifyMapToJSON(issues));
    
  } catch (error) {
    core.setFailed(error.message);
  }
}


