const core = require('@actions/core');
const github = require('@actions/github');
const collectionUtils = require('./utils/collection-utils.js');

module.exports = async function() {
  try {
    const newLabel = core.getInput('label');
    const prefixDelimiter = core.getInput('prefix-delimiter');
    const repoToken = core.getInput('repo-token');
    
    const octokit = github.getOctokit(repoToken);
    const repo = github.context.payload.repository;
    
    const delimiterIndex = newLabel.indexOf(prefixDelimiter);
    core.debug(`New label: ${newLabel}. Prefix delimiter: ${prefixDelimiter}. delimiterIndex: ${delimiterIndex}.`);
   
    if (delimiterIndex < 0) {
      return;
    }
    
    const prefix = newLabel.substring(0, delimiterIndex + prefixDelimiter.length);
    core.debug(`Label prefix: ${prefix}`);
    
    const colorCounts = new Map(); // color => number of labels with that color
   
    await octokit.paginate(octokit.rest.search.labels, {
        repository_id: repo.id,
        q: prefix
    })
    .then(labels => {
      for (const {name, color} of labels) {
        
        if ((name === newLabel) || (!name.startsWith(prefix))) {
          // 1) ignore the new label
          // 2) github search may also return labels 
          // where the prefix is not at the start
          continue;
        }
        
        colorCounts.set(color, (colorCounts.get(color) ?? 0) + 1);
      }
    });
    
    if (colorCounts.size == 0) {
      core.info(`No other labels with the same prefix found. Not changing color of label ${newLabel}`);
      return;
    }
    
    const majorityColor = collectionUtils.getMaximumKeyValuePair(colorCounts)[0];
    core.info(`Assigning color ${majorityColor} to label ${newLabel}`);
    
    octokit.rest.issues.updateLabel({
      owner: repo.owner.login,
      repo: repo.name,
      name: newLabel,
      color: majorityColor
    });
  } catch (error) {
    core.setFailed(error.message);
  }
}