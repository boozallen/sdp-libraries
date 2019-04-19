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
    command = "gradle test"
    stash{
      name = "workspace"
    }
  }
}
