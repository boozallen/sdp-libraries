package libraries.sdp

import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider

void call(ArrayList secretKeys, Closure body) {
    // Handling nulls, library providers can pass config.secrets blindly
    secretKeys = secretKeys ?: []

    if (!(secretKeys instanceof ArrayList)) error "with_secrets - ArrayList expected"

    Map resolvers = [
        AWSCredentialsImpl: { secret ->
            String accessKeyVariable = secret.get('accessKeyVariable', 'AWS_ACCESS_KEY_ID')
            String secretKeyVariable = secret.get('secretKeyVariable', 'AWS_SECRET_ACCESS_KEY')

            [
                $class: 'AmazonWebServicesCredentialsBinding',
                accessKeyVariable: accessKeyVariable,
                secretKeyVariable: secretKeyVariable,
                credentialsId: secret.credentialsId
            ]
        },
        BasicSSHUserPrivateKey: { secret -> sshUserPrivateKey(secret) },
        CertificateCredentialsImpl: { secret -> certificate(secret) },
        DockerServerCredentials: { secret -> dockerCert(secret) },
        FileCredentialsImpl: { secret ->
            String type = secret.remove('type')

            type == 'file' ? file(secret) : zip(secret)
        },
        StringCredentialsImpl: { secret -> string(secret) },
        UsernamePasswordCredentialsImpl: { secret -> secret.variable ? usernameColonPassword(secret) : usernamePassword(secret) }
    ]

    Map secrets = jte.pipelineConfig.secrets ?: [:]

    ArrayList missing = secretKeys
        .findAll { key -> !secrets.containsKey(key) }
        .collect { key -> "- ${key}" }

    if (missing) {
        missing.add(0, "[ERROR] Secret configuration missing secret(s) specified:")

        error missing.join('\n')
    }

    secrets = secretKeys
        .collectEntries { key -> [key, secrets[key]] }

    ArrayList credentials = this.resolveSecrets(secrets, resolvers).values()

    withCredentials(credentials, body)
}

@Validate
void validate() {
    Map secrets = jte.pipelineConfig.secrets ?: [:]

    ArrayList missing = secrets
        .findAll { _, secret -> !secret.containsKey('credentialsId') }
        .collect { name, _ -> "- ${name}" }

    if (missing) {
        missing.add(0, "[ERROR] The following secrets are missing 'credentialsId':")

        error missing.join('\n')
    }

    def checkRequired = { params ->
        ArrayList keys = params.secret.keySet()

        ArrayList unexpected = keys.findAll { !(it in params.allowed) }

        ArrayList errors = params.required
            .findAll { !(it in keys) }
            .collect { "Required key '${it}' not passed" }

        if (unexpected) {
            errors << "Unexpected keys passed: ${unexpected}"
            errors << "Allowed keys: ${params.allowed}"
        }

        return errors
    }

    // credentialsId is checked for above, required = required - credentialsId
    Map resolvers = [
        AWSCredentialsImpl: { secret ->
            Map params = [
                allowed: ['credentialsId', 'accessKeyVariable', 'secretKeyVariable'],
                required: [],
                secret: secret
            ]

            checkRequired(params)
        },
        BasicSSHUserPrivateKey: { secret ->
            Map params = [
                allowed: ['credentialsId', 'keyFileVariable', 'usernameVariable', 'passwordVariable'],
                required: ['keyFileVariable'],
                secret: secret
            ]

            checkRequired(params)
        },
        DockerServerCredentials: { secret ->
            Map params = [
                allowed: ['credentialsId', 'variable'],
                required: ['variable'],
                secret: secret
            ]

            checkRequired(params)
        },
        CertificateCredentialsImpl: { secret ->
            Map params = [
                allowed: ['credentialsId', 'aliasVariable', 'keystoreVariable', 'passwordVariable'],
                required: ['keystoreVariable'],
                secret: secret
            ]

            checkRequired(params)
        },
        FileCredentialsImpl: { secret ->
            Map params = [
                allowed: ['credentialsId', 'variable', 'type'],
                required: ['variable', 'type'],
                secret: secret
            ]

            errors = checkRequired(params)

            ArrayList allowedTypes = ['file', 'zip']

            if (secret.type && !(secret.type in allowedTypes)) errors << "The key 'type' must be in ${allowedTypes}"

            errors
        },
        StringCredentialsImpl: { secret ->
            Map params = [
                allowed: ['credentialsId', 'variable'],
                required: ['variable'],
                secret: secret
            ]

            checkRequired(params)
        },
        UsernamePasswordCredentialsImpl: { secret ->
            Map params = [
                allowed: ['credentialsId', 'variable', 'usernameVariable', 'passwordVariable'],
                required: [],
                secret: secret
            ]

            ArrayList errors = checkRequired(params)

            boolean colonSecret     = 'variable' in secret
            boolean seperatedSecret = ('usernameVariable' in secret || 'passwordVariable' in secret)
            boolean userPassSecret  = ('usernameVariable' in secret && 'passwordVariable' in secret)

            if (secret.size() == 1 || (colonSecret && seperatedSecret)) {
                errors << "Either 'variable' or 'usernameVariable' and 'passwordVariable' should be specified"
            }
            else if (!userPassSecret) {
                errors << "Both 'usernameVariable' and 'passwordVariable' must be specified"
            }

            return errors
        },
        NullObject: { secret -> ["Credential '${secret.credentialsId}' not found"] },
        Unknown: { secret -> ["Credential '${secret.credentialsId}' is of an unsupported type"] }
    ]

    ArrayList errors = this.resolveSecrets(secrets, resolvers)
        .findAll { _, errs -> errs }
        .collect { key, errs ->
            errs = errs.collect { "- ${it}" }
            errs.add(0, "Secret '${key}' has errors:")

            return errs.join('\n')
        }

    if (errors) {
        errors.add(0, '[ERROR] Secrets have the following errors:')

        error errors.join('\n')
    }
}

Map resolveSecrets(Map secrets, Map resolvers) {
    ArrayList credentials = CredentialsProvider.lookupCredentials(Credentials, Jenkins.get(), null, null)

    secrets.collectEntries { key, secret ->
        def cred = credentials.find { cred -> cred.id.equals(secret.credentialsId) }

        String clazz = cred
            .getClass()
            .toString()
            .tokenize('.')
            .pop()

        def resolver = resolvers.get(clazz, resolvers['Unknown'])

        [key, resolver(secret)]
    }
}
