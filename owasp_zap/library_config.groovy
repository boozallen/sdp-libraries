fields{
  optional{
    target = ~/^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#\[\]@!\$&'\(\)\*\+,;=.]+$/ url
    vuln_threshold = ["Ignore", "Low", "Medium", "High", "Informational"]
  }
}
