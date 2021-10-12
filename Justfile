image := "mkdocs-local"

# Print recipes
help:
  just --list --unsorted 

# Run unit tests
coverage := "false"
test class="*":
  #!/usr/bin/env bash
  set -euxo pipefail
  coverage=$([[ {{coverage}} == "true" ]] && echo "jacocoTestReport" || echo "")
  ./gradlew test --tests '{{class}}' $coverage

# Uses npm-groovy-lint to lint the libraries
lint: 
  docker run --rm \
  -u "$(id -u):$(id -g)" \
  -w=/tmp \
  -v "$PWD":/tmp \
  nvuillam/npm-groovy-lint -f "libraries/**/*.groovy" -o json

# Build the docs container image
buildImage:
  docker build resources -t {{image}}

# Build the documentation
build:
  docker run --rm -v $(pwd):/docs {{image}} build

# Live reloading of the documentation
serve: buildImage build
  #!/bin/bash
  docker run --rm -it -p 8000:80 -v $(pwd)/site:/usr/share/nginx/html --name local-docs -d nginx
  trap "just clean" INT
  watchexec --exts md,yml,pages just build

# Cleanup the docs and target directory
clean: 
  docker rm -f local-docs
  rm -f site
  ./gradlew clean

# Create a library
create libName:
  mkdir -p libraries/{{libName}}/{steps,src,resources,test}
  cp resources/README.template.md libraries/{{libName}}/README.md

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