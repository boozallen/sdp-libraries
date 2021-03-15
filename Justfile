# whether or not to measure test coverage
coverage := "true"
# the docs output directory
docsDir := "docs/html"
# the Antora playbook file
playbook := "docs/antora-playbook-local.yml"

# Print recipes
help:
  just --list --unsorted 

# Remove local caches
clean: 
  ./gradlew clean
  rm -rf {{docsDir}}

# Run unit tests
test class="*":
  #!/usr/bin/env bash
  set -euxo pipefail
  coverage=$([[ {{coverage}} == "true" ]] && echo "jacocoTestReport" || echo "")
  ./gradlew test --tests '{{class}}' $coverage

# Build the local Antora documentation
docs:
  docker run \
  -t --rm \
  -v ~/.git-credentials:/home/antora/.git-credentials \
  -v $(pwd):/app -w /app \
  docker.pkg.github.com/boozallen/sdp-docs/builder \
  generate --generator booz-allen-site-generator \
  --to-dir {{docsDir}} \
  {{playbook}}

# Cut a release of the SDP Libraries
release version branch=`git branch --show-current`: 
  #!/usr/bin/env bash
  if [[ ! "{{branch}}" == "main" ]]; then 
    echo "You can only cut a release from the 'main' branch."
    echo "Currently on branch '{{branch}}'"
    exit 1
  fi
  # cut a release branch
  git checkout -B release/{{version}}
  # bump the version in relevant places
  sed -ie "s/^version:.*/version: '{{version}}'/g" docs/antora.yml
  git add build.gradle docs/antora.yml
  git commit -m "bump version to {{version}}"
  git push --set-upstream origin release/{{version}}
  # push a tag for this release
  git tag {{version}}
  git push origin refs/tags/{{version}}