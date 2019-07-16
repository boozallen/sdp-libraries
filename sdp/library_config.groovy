fields{
  required{
    images{
      registry = ~/^http[a-zA-Z0-9\-_\.:\/]+$/
      cred = ~/^[a-zA-Z0-9\-_\.]+$/
    }
  }
  optional{
    images{
      repository = ~/^[a-zA-Z0-9\-_\.\/]+$/
      docker_args = String
    }
  }
}
