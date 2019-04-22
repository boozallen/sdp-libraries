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
      registry = "https://docker-registry-default.scv:5000"
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
    command = "export JAVA_OPTS='org.gradle.jvmargs=-Xmx3g -XX:MaxPermSize=2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
' && gradle test"
    stash{
      name = "workspace"
    }
  }
}
