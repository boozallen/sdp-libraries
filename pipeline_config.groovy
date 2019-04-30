libraries {
  github_enterprise
  sdp {
    images{
      registry = "https://docker-registry.default.svc:5000"
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
    docker_args = "--oom-kill-disable -m 3500m"
    command = "./gradlew test"
    stash{
      name = "workspace"
    }
  }
}
