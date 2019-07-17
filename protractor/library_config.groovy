fields{
  optional{
    url = ~/^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#\[\]@!\$&'\(\)\*\+,;=.]+$/
    enforce = Boolean
    config_file = String // file path
  }
}
