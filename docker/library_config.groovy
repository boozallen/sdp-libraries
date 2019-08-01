fields{
  required{
    registry = ~/^(?!http)[a-zA-Z0-9\-_\.]+(:\d+)?(\/[a-zA-Z0-9\-_\.]+)*$/
    cred = ~/^[a-zA-Z0-9\-_\.]+$/
  }
  optional{
    build_strategy = ["dockerfile", "docker-compose", "modules"]
    repo_path_prefix = ~/^[a-zA-Z0-9\-_\.]+$/
  }
}
