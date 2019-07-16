fields{
  required{
    images{
      registry = String
      cred = String
    }
  }
  optional{
    images{
      repository = String
      docker_args = String
    }
  }
}
