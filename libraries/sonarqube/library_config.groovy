fields{
    required{}
    optional{
        wait_for_quality_gate = Boolean
        enforce_quality_gate = Boolean
        credential_id = String
        installation_name = String
        stage_display_name = String
        timeout_duration = Number
        timeout_unit = [ "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS" ]
        cli_parameters = List
        unstash = List
        project_key = String //dotnet specific
        test_output_dir = String //dotnet specific
        coverage_settings_file = String //dotnet specific
    }
}