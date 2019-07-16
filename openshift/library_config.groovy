fields{
  optional{
    helm_configuration_repository = ~/^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#\[\]@!\$&'\(\)\*\+,;=.]+$/
    helm_configuration_repository_credential = ~/^[a-zA-Z0-9\-_\.]+$/
    tiller_namespace = ~/^[a-zA-Z0-9\-_\.]+$/
    tiller_credential = ~/^[a-zA-Z0-9\-_\.]+$/
    url = ~/^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#\[\]@!\$&'\(\)\*\+,;=.]+$/
    promote_previous_image = Boolean
  }
}
