import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import jenkins.model.JenkinsLocationConfiguration
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.util.Secret

// Configurar URL de Jenkins (necesario para webhooks)
def locationConfig = JenkinsLocationConfiguration.get()
locationConfig.setUrl('http://jenkins:8080/')
locationConfig.save()

def instance = Jenkins.getInstance()

// La configuracion del servidor SonarQube se hace via JCasC
// (archivo casc.yaml montado en /var/jenkins_home/casc_configs/)
println "NOTA: Servidor SonarQube se configura via JCasC (casc.yaml)"

// Crear credencial de SonarQube si no existe
def credentialsStore = instance.getExtensionList(
    com.cloudbees.plugins.credentials.SystemCredentialsProvider.class
)[0].getStore()

def sonarTokenId = 'sonarqube-token'
def existingCred = credentialsStore.getCredentials(Domain.global()).find { it.id == sonarTokenId }

if (existingCred != null) {
    credentialsStore.removeCredentials(Domain.global(), existingCred)
    println "Credencial antigua '${sonarTokenId}' eliminada para actualizar"
}

    def sonarToken = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        sonarTokenId,
        'Token de SonarQube para analisis de codigo',
        Secret.fromString('squ_c7976c4aadd0d1fcd5f9a7f5866269d1214da09a')
    )
    credentialsStore.addCredentials(Domain.global(), sonarToken)
println "Credencial '${sonarTokenId}' creada/actualizada exitosamente"
// Configurar variable de entorno global SONAR_TOKEN (fallback si la credencial falla)
def globalNodeProperties = instance.getGlobalNodeProperties()
def envVarsNodePropertyList = globalNodeProperties.getAll(
    hudson.slaves.EnvironmentVariablesNodeProperty.class
)
def envVarsNodeProperty = envVarsNodePropertyList.isEmpty()
    ? new hudson.slaves.EnvironmentVariablesNodeProperty()
    : envVarsNodePropertyList.get(0)

if (envVarsNodePropertyList.isEmpty()) {
    globalNodeProperties.add(envVarsNodeProperty)
}
envVarsNodeProperty.getEnvVars().put('SONAR_TOKEN', 'squ_c7976c4aadd0d1fcd5f9a7f5866269d1214da09a')
envVarsNodeProperty.getEnvVars().put('SONAR_HOST_URL', 'http://sonarqube:9000')
println "Variables de entorno globales configuradas"

// Leer contenido de un archivo Jenkinsfile
String readJenkinsfile(String path) {
    def file = new File(path)
    if (!file.exists()) {
        println "WARNING: Jenkinsfile not found at ${path}"
        return "pipeline { agent any; stages { stage('Error') { steps { error 'Jenkinsfile not found: ${path}' } } } }"
    }
    return file.text
}

// Crear pipeline job a partir de un Jenkinsfile local
// Si ya existe, actualiza su definicion
def createJobFromFile(jenkinsInstance, String jobName, String jenkinsfilePath) {
    def pipelineScript = readJenkinsfile(jenkinsfilePath)
    def existing = jenkinsInstance.getItem(jobName)

    if (existing != null) {
        // Actualizar job existente
        existing.definition = new CpsFlowDefinition(pipelineScript, true)
        existing.save()
        println "Job actualizado: ${jobName} (desde ${jenkinsfilePath})"
        return
    }

    def job = jenkinsInstance.createProject(WorkflowJob.class, jobName)
    job.definition = new CpsFlowDefinition(pipelineScript, true)
    job.save()
    println "Job creado: ${jobName} (desde ${jenkinsfilePath})"
}

// Los Jenkinsfiles estan montados via volumen en /code/
def codeBase = '/code'

// Pipeline 1: empleados-service (Java/Gradle)
createJobFromFile(instance, 'empleados-service-ci', "${codeBase}/empleados-service/Jenkinsfile")
// Pipeline 2: python-healthcheck-service (Python/Flask)
createJobFromFile(instance, 'python-healthcheck-service-ci', "${codeBase}/python-healthcheck-service/Jenkinsfile")
// Pipeline 3: e2e-tests (Cucumber/Java)
createJobFromFile(instance, 'e2e-tests-ci', "${codeBase}/e2e-tests/Jenkinsfile")
// Pipeline 4: hello-world (verificacion)
def existingHello = instance.getItem('hello-world')
if (existingHello == null) {
def helloJob = instance.createProject(WorkflowJob.class, 'hello-world')
helloJob.definition = new CpsFlowDefinition('''
pipeline {
    agent any
    stages {
        stage('Verificacion') {
            steps {
                echo 'Jenkins esta correctamente configurado'
                sh 'docker --version'
                sh 'docker ps'
            }
        }
    }
}
''', true)
helloJob.save()
println 'Job creado: hello-world'
} else {
    println "Job 'hello-world' ya existe, omitiendo..."
}

instance.save()
println ''
println '============================================'
println 'Todos los jobs han sido creados exitosamente'
println '============================================'
println ''
println 'Jobs disponibles:'
println '  - empleados-service-ci'
println '  - python-healthcheck-service-ci'
println '  - e2e-tests-ci'
println '  - hello-world'
println ''
println 'Variables de entorno globales:'
println '  - SONAR_TOKEN (configurado)'
println '  - SONAR_HOST_URL = http://sonarqube:9000'
println ''
println 'Webhook de SonarQube configurado:'
println '  - URL: http://jenkins:8080/sonarqube-webhook/'
println ''
println 'Quality Gate personalizado: ProyectoQualityGate (cobertura >= 70%)'
println 'Proyectos de SonarQube creados:'
println '  - empleados-service'
println '  - python-healthcheck-service'
println ''
