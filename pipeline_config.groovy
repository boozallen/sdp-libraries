libraries {
  github_enterprise
  docker {
    build_strategy = "modules"
    registry = "docker-registry.default.svc:5000"
    cred = "openshift-docker-registry"
    repo_path_prefix = "keegan-sdp"
  }
  sdp {
    images{
      registry = "https://docker-registry-default.svc:5000"
      repo = "keegan-sdp"
      cred = "openshift-docker-registry"
    }
  }
  sonarqube
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
