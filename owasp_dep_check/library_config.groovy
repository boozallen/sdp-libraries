fields{
  required{

  }
  optional{
    scan_target = String //some filepath
    exclude_dirs = String //comma-separated list
    cvss_threshold = ~/^(pass|\d+)$/
    image_version = ~/^[a-zA-Z0-9][a-zA-Z0-9_\.\-]*$/
    report_format = ["XML", "HTML", "CSV", "JSON", "VULN", "ALL"]
  }
}
