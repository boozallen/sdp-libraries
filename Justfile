coverage := "true"
image := "mkdocs-local"

#########################
# Misc Recipes
#########################

# Print recipes
help:
  just --list --unsorted 

# Cleanup the docs and target directory
clean: 
  docker rm -f local-docs
  rm -f site
  ./gradlew clean

# Create a library
create libName:
  mkdir -p libraries/{{libName}}/{steps,src,resources,test}
  LIB={{libName}} envsubst < resources/docs/README.template.md > libraries/{{libName}}/README.md

###################
# Code Recipes
###################

# Run unit tests
test class="*":
  #!/usr/bin/env bash
  set -euxo pipefail
  coverage=$([[ {{coverage}} == "true" ]] && echo "jacocoTestReport" || echo "")
  ./gradlew test --tests '{{class}}' $coverage

# Uses npm-groovy-lint to lint the libraries
lint-code: 
  docker run --rm \
  -u "$(id -u):$(id -g)" \
  -w=/tmp \
  -v "$PWD":/tmp \
  nvuillam/npm-groovy-lint -p ./libraries -f **/*.groovy -i "**/test/* -o json"

########################
# Documentation Recipes
########################

# Build the docs container image
buildImage:
  docker build resources/docs -t {{image}}

# Build the documentation
build:
  docker run --rm -v $(pwd):/docs {{image}} build

# serve the docs locally for development
serve: buildImage
  docker run --rm -p 8000:8000 -v $(pwd):/docs {{image}} serve -a 0.0.0.0:8000 --watch-theme

# Lint the documentation
lint-docs: lint-prose lint-libraries lint-markdown

# use Vale to lint the prose of the documentation
lint-prose:
  docker run -v $(pwd):/app -w /app jdkato/vale docs

lint-libraries:
  docker run -v $(pwd):/app -w /app jdkato/vale libraries

# use markdownlit to lint the docs
lint-markdown: 
  docker run -v $(pwd):/app -w /app davidanson/markdownlint-cli2:0.3.1 "docs/**/*.md" "libraries/**/*.md"

######################

release version: 
  #!/usr/bin/env bash
  # make sure release is done from main
  branch=$(git branch --show-current)
  if [[ ! "${branch}" == "main" ]]; then 
    echo "You can only cut a release from the 'main' branch."
    echo "Currently on branch '${branch}'"
    exit 1
  fi

  # cut a release branch
  git checkout -B release/{{version}}

  # bump the version in relevant places
  git commit -m "bump version to {{version}}"
  git push --set-upstream origin release/{{version}}

  # push a tag for this release
  git tag {{version}}
  git push origin refs/tags/{{version}}

  # push the docs for this release
  docker run --rm \
  -v $(pwd):/docs \
  -v ~/.gitconfig:/root/.gitconfig \
  -v ~/.git-credentials:/root/.git-credentials \
  --entrypoint mike \
  {{image}} deploy --push --update-aliases {{version}} latest

  docker run --rm \
  -v $(pwd):/docs \
  -v ~/.gitconfig:/root/.gitconfig \
  -v ~/.git-credentials:/root/.git-credentials \
  --entrypoint mike \
  {{image}} set-default -p latest

  # go back to main 
  git checkout main

delete-release version: 
  git push origin --delete release/{{version}}
  git tag -d {{version}}
  git push --delete origin {{version}}
  docker run --rm \
  -v $(pwd):/docs \
  -v ~/.gitconfig:/root/.gitconfig \
  -v ~/.git-credentials:/root/.git-credentials \
  --entrypoint mike \
  {{image}} delete -p -f {{version}}