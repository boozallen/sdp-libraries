fields {
  required {
    npm_script = String
    report_path = String
  }
  optional {
    test_repo = String
    branch = String
    container_image = String
    container_registry = String
    container_registry_creds = String
  }
}
