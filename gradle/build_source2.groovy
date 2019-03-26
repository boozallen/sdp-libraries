def call(){
    stage "Building Gradle Project", {
        node{
            unstash "workspace"

            sh './gradlew clean build'

            stash "workspace"
        }
    }
}