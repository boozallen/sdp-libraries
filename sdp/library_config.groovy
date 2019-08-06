fields{
  required{
    images{
      registry = ~/^http(s)?:\/\/[a-zA-Z0-9\-_\.]+(:\d+)?$/
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
