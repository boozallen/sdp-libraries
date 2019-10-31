fields{
  optional{
    credential_id = ~/^[a-zA-Z0-9\-_\.]+$/
    enforce_quality_gate = Boolean
    build_step = String
    require_build_step = Boolean
  }
}
