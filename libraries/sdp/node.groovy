/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/**
 * An implementtaion of custom node step to be used by SDP library to run a step on a particular node.
 */
void call(String label = null, Closure body){
    LinkedHashMap bodyConfig = [:]
    try{
        bodyConfig = body.config
    }catch(MissingPropertyException ex){
        // node invoked from outside a library step
    }

    processNodeCall(label, body, bodyConfig)
}

/**
 * A helper method to take on incoming calls to "node" step. "forceUseDefault" is
 * true if this method is called on account of a "node" call from outside the library and false
 * if called by a library step.
 */
void processNodeCall(String label, Closure body, LinkedHashMap bodyConfig){
    String agentType = bodyConfig.agentType ?: config.agentType ?: "generic"
    if(!(agentType in ["kubernetes", "docker", "generic"])){
        error "The specified agentType must be one of ['kubernetes', 'docker', 'generic'].  Found '${agentType}'."
    }

    switch(agentType){
      case "kubernetes":
        handleKubernetesNode(label, body, bodyConfig)
        break
      case "docker":
        handleDockerNode(label, body, bodyConfig)
        break
      case "generic":
        handleGenericNode(label, body, bodyConfig)
        break
    }
}

/** 
 * implements the node step when the agentType is kubernetes
 */
void handleKubernetesNode( String label, Closure body, LinkedHashMap bodyConfig){

    String podYaml = getPodTemplate(label, bodyConfig)
    println "found pod yaml:"
    println podYaml

    podTemplate(
        yaml: getPodTemplate(label, bodyConfig),
        cloud: bodyConfig.podSpec?.namespace ?: config.podSpec?.namespace ?: "kubernetes", 
        namespace: bodyConfig.podSpec?.namespace ?: config.podSpec?.namespace ?: "default",
        workingDir: "/home/jenkins/agent",
    ){
        steps.node(POD_LABEL){
            container('sdp-container', body)
        }
    }
}

/**
 * builds the pod YAML for the kubernetes agentType
 */
String getPodTemplate(String label, LinkedHashMap bodyConfig) {
    String img = getImage(label, "kubernetes", bodyConfig)
    String pullSecret = getRegistryCred("kubernetes", bodyConfig)

    return """
    apiVersion: v1
    kind: Pod
    metadata:
        name: sdp-agent
    spec:
        containers:
        - image: ${img}
        imagePullPolicy: IfNotPresent 
        ${pullSecret ? "imagePullSecrets: ${pullSecret}" : ""}
        name: sdp-container
        tty: true
        workingDir: /home/jenkins/agent
    """.stripIndent()
}

/**
 * implements the node step when the agentType is docker
 */
void handleDockerNode(String label, Closure body, LinkedHashMap bodyConfig){
    String nodeLabel = bodyConfig.nodeLabel ?: config.nodeLabel ?: ""
    String imgRegistry = getRegistry("docker", bodyConfig)
    String imgRegistryCred = getRegistryCred("docker", bodyConfig)
    String img = getImage(label, "docker", bodyConfig)
    String args = bodyConfig.images?.docker_args ?: config.images?.docker_args ?: ""

    Closure imageInside = { docker.image(img).inside(args, body) }
    
    steps.node(nodeLabel){
        if(imgRegistry) docker.withRegistry(imgRegistry, imgRegistryCred, imageInside)
        else imageInside()
    }
}

/*
 * implements the node step when the agentType is generic
 */
void handleGenericNode(String label, Closure body, LinkedHashMap bodyConfig){
   String nodeLabel = bodyConfig.nodeLabel ?: config.nodeLabel ?: ""
   steps.node(nodeLabel, body)
}


/**
 * determines what image to use for the container-based agentTypes
 */
String getImage(String label, String agentType, LinkedHashMap bodyConfig){
    String key
    if(agentType.equals("docker")){
        key = "images"
    } else if (agentType.equals("kubernetes")){
        key = "podSpec"
    }

    String img = label ?: bodyConfig[key]?.img ?: config[key]?.img
    if(!img){
        error "You must define the image to use"
    }
    
    String registry = getRegistry(agentType, bodyConfig)
    return registry ? "${registry}/${img}" : img
}

/**
 * determines the image registry from which to pull the image for the container-based agentTypes
 */
String getRegistry(String agentType, LinkedHashMap bodyConfig){
    String key
    if(agentType.equals("docker")){
        key = "images"
    } else if (agentType.equals("kubernetes")){
        key = "podSpec"
    }

    return bodyConfig[key]?.registry ?: config[key]?.registry ?: ""
}

/**
 * determines the jenkins credential ID to use for the container-based agentTypes
 */
String getRegistryCred(String agentType, LinkedHashMap bodyConfig){
    String key
    if(agentType.equals("docker")){
        key = "images"
    } else if (agentType.equals("kubernetes")){
        key = "podSpec"
    }
    
    return bodyConfig[key]?.cred ?: config[key]?.cred ?: ""
}