fields{
    optional{
        grype_container = String
        report_format = ["json", "table", "cyclonedx", "template"]
        fail_on_severity = ["none", "negligible", "low", "medium", "high", "critical"]
        grype_config = String
    }
}
