void call(Closure body){

    if (config.agent && !(config.agent instanceof String)){
        error """
        libraries{
            sdp{
                agent = ${config.agent} <-- must be a string

            }
        }
        """
    }

    steps.node(config.agent ?: ""){
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = this
        body()
    }
}
