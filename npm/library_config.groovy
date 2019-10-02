fields{
  optional{
    images{
      registry = ~/^http(s)?:\/\/[a-zA-Z0-9\-_\.]+(:\d+)?$/
      cred = ~/^[a-zA-Z0-9\-_\.]+$/
      repository = ~/^[a-zA-Z0-9\-_\.\/]+$/
      docker_args = String
    }
  }
}