package libraries.sonarqube

public class SonarqubeHelper {
  
  Script setupMocks(Script sonarqubeScript) {   
    /* Mock methods that call SonarGlobalConfiguration */
    sonarqubeScript.metaClass.validateInstallationExists = { s -> 
      return true;
    }
    sonarqubeScript.metaClass.fetchCredentialFromInstallation = { s ->
      return "creds";
    }
    sonarqubeScript.metaClass.determineCredentialType = { String cred_id ->
      return [ string(credentialsId: "creds", variable: 'sq_token') ] 
    }
    sonarqubeScript.metaClass.determineCredentialType = { String cred_id ->
      return [ string(credentialsId: "creds", variable: 'sq_token') ] 
    }
    sonarqubeScript.metaClass.waitForQualityGate = { ->
      return LinkedHashMap[status:'OK']
    }

    return sonarqubeScript
  }
}
