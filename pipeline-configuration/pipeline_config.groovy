libraries{
    merge = true
    sonarqube
    webhint
    sdp{
    images{
      registry = "https://docker.pkg.github.com"
      repository = "boozallen/sdp-images"
      cred = "github"
    }
  }
}
