---
description: Allows you to map a branching strategy to specific pipeline actions when using Public GitHub, GitLab or GitHub Enterprise
---

# Git

This library is unique in that rather than provide functional step implementations, it provides methods that help with the business logic defined within pipeline templates.

**Note** It also provides additional functionality that can be useful for library developers to get scm metadata or interact with a remote Gitlab or GitHub repository.

## Configuration

---

```groovy
libraries{
  git{
    github
    github_enterprise
    gitlab{
      connection (optional) = String // for gitlab_status
      job_name (optional) = String
      job_status (optional) =  "pending" or "running" or "canceled" or "failed" or "success".
    }
  }
}
```

## Pipeline Template Business Logic

---

The Git library contributes some helper methods to help with pipeline template orchestration.
You can achieve fine grained control over what happens when in response to different Git events such as commits, merge requests, and merges.

Git Flow Helper Methods

| Method | Build Cause |
| ----------- | ----------- |
| on_commit | A direct commit to a branch |
| on_merge_request | A merge request was created or a developer pushed a commit to the source branch |
| on_change | A combination of `on_commit` and `on_merge_request` |
| on_merge | A merge request was merged into the branch |

These methods take named parameters `to` and `from` indicating direction of the git whose value is a regular expression to compare the branch names against.

SDP recommends defining keywords for branch name regular expressions:

```groovy
keywords{
  master = /^[Mm]aster$/
  develop = /^[Dd]evelop(ment\|er\|)$/
  hotfix = /^[Hh]ot[Ff]ix-/
  release = /^[Rr]elease-(d+.)*d$/
}
```

**Note** These branch name regular expressions aren't a part of the Git library but rather leveraged by defining Keywords in the Pipeline Configuration File.

## SCM Specific Methods

---

Gitlab Methods

| Method | Explanation |
| ----------- | ----------- |
| gitlab_status | Track Jenkins pipeline jobs in Gitlab |

## Example Pipeline Templates

---

**Full example using keywords**

```groovy
on_commit{
  gitlab_status("connection1", "service-account", "running")
  continuous_integration()
  gitlab_status("connection1", "service-account", "success")
}

on_pull_request to: develop, {
  gitlab_status("connection2", "service-account", "pending")
  continuous_integration()
  gitlab_status("connection2", "service-account", "running")
  deploy_to dev
  parallel "508 Testing": { accessibility_compliance_test() },
          "Functional Testing": { functional_test() },
          "Penetration Testing": { penetration_test() }
  deploy_to staging
  performance_test()
  gitlab_status("connection2", "service-account", "success")
}

on_merge to: master, from: develop, {
  gitlab_status("connection", "service-account2", "running")
  deploy_to prod
  smoke_test()
  gitlab_status("connection", "service-account2", "success")
}
```

**Example using regular expressions directly**

```groovy
on_commit to: /^[Ff]eature-.*/, {
  // will be triggered on feature branches
}
on_merge_request from: /^[Ff]eature-.*/, to: develop, {
  // will be triggered on PR's from feature to develop
}
```

**Example using on_change**

```groovy
on_change{
  // do CI on every commit or PR
  continuous_integration()
}
on_pull_request to: master, {
  // do some stuff on PR to master
}
on_merge to: master, {
  // PR was merged into master
}
```

## External Dependencies

---

* `gitlab-branch-source-plugin:1.4.4` if using gitlab

## Troubleshooting

---

## FAQ

---
