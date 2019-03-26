def call(){
    stage "Building Gradle Project", {
        node{
            unstash "workspace"

            sh 'gradle clean build'

            stash "workspace"
        }
    }
}