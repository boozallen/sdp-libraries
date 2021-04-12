fields{
    required{
        cred = String
        anchore_engine_url = String	
    }
    optional{
	image_wait_timeout = int
	policy_id = String	
	archive_only = Boolean
	bail_on_fail = Boolean
	perform_vulnerability_scan = Boolean
	perform_policy_evaluation = Boolean
    }
}
