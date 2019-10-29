def call(path, region){
  inside_sdp_image("aws") {
    assumeRole()
    def additionalConfig = [:]
    def response = sh(script: "aws ssm get-parameters-by-path --recursive --path ${path} --region ${region}", returnStdout: true).trim()
    def params = readJSON(text: response).Parameters

    params.findAll { param ->
      def res = sh(script: "aws ssm list-tags-for-resource --resource-type Parameter --resource-id ${param.Name} --region ${region}", returnStdout: true).trim()
      def tagList = readJSON(text: res).TagList
      return tagList.find { it.Key.equals("sdp") }?.Value
    }.each { param ->
      additionalConfig[param.Name.split("/").last()] = param.Value
    }
    return additionalConfig
  }
}

