fields{
    optional{
        gitlab{
            connection = String
            job_name = String
            job_status = String
        }
    }
    required{
        distribution = [ "github", "github_enterprise", "gitlab" ]
    }
}