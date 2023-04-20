# Adding a PR label

1. Install [github-label-sync](https://github.com/Financial-Times/github-label-sync)
1. Create a [personal github access token](https://github.com/settings/tokens)
    * With `repo` permissions
    * With access to the Camunda organization
1. Add the label to [pr-labels.yml]()
1. Sync the label with `camunda/camunda-bpm-platform`:
    1. Set an environment variable `GITHUB_ACCESS_TOKEN` with your github token
    1. `github-label-sync --dry-run --labels ./.github/pr-labels.yml --allow-added-labels camunda/camunda-bpm-platform`
        * Only remove `--allow-added-labels` when you are really sure. When not set, other labels in the repository will be deleted. There is no way to restore the label assignment to issues once deleted.
        * **Never** skip the dry run, it's easy to mess things up here.
    1. Review the dry-run output
    1. Run the command again without `--dry-run`
1. Sync the labels by applying the same procedure with repositories:
    * `camunda/camunda-bpm-platform-maintenance`
    * `camunda/camunda-bpm-platform-ee`
    * `camunda/camunda-bpm-platform-ee-maintenance`
    * `camunda/camunda-bpm-rpa-bridge-ee`
  
Read https://confluence.camunda.com/display/AP/Github+labels for how to change other labels and how we overall work with labels.