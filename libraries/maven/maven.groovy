package libraries.maven

void call(){

}

void run(Map params = [:], ArrayList<String> phases) {
    if (!config.mavenId) {
        error "Must supply the installed Maven version's ID"
    }
    this.run(phases, params.get('goals', []) as ArrayList<String>, params.get('properties', [:]) as Map<String, String>, params.get('profiles', []) as ArrayList<String>)
}

// Run maven command with installed maven version
void run(ArrayList<String> phases, ArrayList<String> goals, Map<String, String> properties, ArrayList<String> profiles) {
    stage("Maven") {
        withMaven(maven: config.mavenId) {
            String command = "mvn "
            if (!phases) {
                error "Must supply phase for Maven"
            }
            phases.each { phase -> command += "${phase} "}

            if (goals) {
                goals.each { goal -> command += "${goal} " }
            }

            if (properties) {
                properties.each { propertyName, value -> command += "-D${propertyName} "
                    if (value != null) {
                        command += "= ${value} "
                    }
                }
            }

            if (profiles) {
                command += "-P"
                String joined = profiles.join(",")
                command += joined
            }

            sh command
        }
    }
}