fields{
  optional{
    target = ~/^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#\[\]@!\$&'\(\)\*\+,;=.]+$/
    vuln_threshold = ["Ignore", "Low", "Medium", "High", "Informational"]
  }
}
