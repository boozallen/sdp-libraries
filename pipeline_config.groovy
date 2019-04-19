libraries {

}

keywords {

}

stages {

}

steps {
  unit_test {
    stage = "Unit Test"
    image = "gradle"
    command = "make test"
    stash{
      name = "workspace"
    }
  }
}
