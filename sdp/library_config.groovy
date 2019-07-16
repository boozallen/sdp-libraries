fields{
  required{
    images.registry = String
    images.cred = String
  }
  optional{
    images.repository = String
    images.docker_args = String
  }
}
