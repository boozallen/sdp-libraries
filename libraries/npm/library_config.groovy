fields {
    required {}
    optional {
        npm_registry_credentials = {
            repo_name = String
            repo_url = String
            repo_auth = String
            credential_id = String
        }[]
    }
}