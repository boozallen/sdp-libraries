libraries {
  github_enterprise
  docker {
    build_strategy = "modules"
  }
}

keywords {

}

stages {

}

steps {
  unit_test {
    stage = "Unit Test"
    image = "gradle"
    command = "gradle test"
    stash{
      name = "workspace"
    }
  }
}
