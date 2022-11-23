fields{
    required{
    }
    optional{
        image = String
        unity_credential_id = String
        unity_serial_id = String
        unity_app = Boolean
        run_sca = Boolean
        activate_license_parameters = String[]
        build_unity_parameters =  String[]
        workspace_name = String
        // above was previously in required
        wait_for_quality_gate = Boolean
        enforce_quality_gate = Boolean
        credential_id = String
        sonar_token = String
        stage_display_name = String
        timeout_duration = Number
        timeout_unit = [ "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS" ]
        cli_parameters = List
        unstash = List
    }
}