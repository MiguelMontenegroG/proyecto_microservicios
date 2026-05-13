import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.FreeStyleProject
import jenkins.model.JenkinsLocationConfiguration

def jenkins = Jenkins.get()

// Configurar Jenkins URL
def locationConfig = JenkinsLocationConfiguration.get()
locationConfig.setUrl('http://localhost:9090/')
locationConfig.save()
println "Jenkins URL configurado: http://localhost:9090/"

// ─────────────────────────────────────────────
// 1. Seguridad: crear usuario admin si no existe
// ─────────────────────────────────────────────
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
if (!hudsonRealm.getAllUsers().find { it.id == 'admin' }) {
    hudsonRealm.createAccount('admin', 'admin123')
    jenkins.setSecurityRealm(hudsonRealm)

    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    jenkins.setAuthorizationStrategy(strategy)
}

// ─────────────────────────────────────────────
// 2. Crear pipelines leyendo Jenkinsfiles del codigo fuente montado
// ─────────────────────────────────────────────

String readJenkinsfile(String path) {
    def file = new File(path)
    if (!file.exists()) {
        println "WARNING: Jenkinsfile not found at ${path}"
        return "pipeline { agent any; stages { stage('Error') { steps { error 'Jenkinsfile not found: ${path}' } } } }"
    }
    return file.text
}

def createJobFromFile(jenkinsInstance, String jobName, String jenkinsfilePath) {
    def pipelineScript = readJenkinsfile(jenkinsfilePath)
    def existing = jenkinsInstance.getItem(jobName)

    if (existing != null) {
        // Actualizar job existente con la nueva definicion
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

def codeBase = '/code'

// Hello-world pipeline de prueba (verificacion Docker)
def helloWorldExisting = jenkins.getItem('hello-world')
if (helloWorldExisting == null) {
def helloWorldJob = jenkins.createProject(WorkflowJob.class, 'hello-world')
helloWorldJob.definition = new CpsFlowDefinition("""
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
""", true)
helloWorldJob.save()
println "Job creado: hello-world"
    } else {
    println "Job hello-world ya existe, se omite"
}

createJobFromFile(jenkins, 'empleados-service-ci', "${codeBase}/empleados-service/Jenkinsfile")
createJobFromFile(jenkins, 'python-healthcheck-service-ci', "${codeBase}/python-healthcheck-service/Jenkinsfile")
createJobFromFile(jenkins, 'e2e-tests-ci', "${codeBase}/e2e-tests/Jenkinsfile")

// ─────────────────────────────────────────────
// 4. Credencial placeholder para SonarQube token
// ─────────────────────────────────────────────
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.util.Secret

def credStore = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()
def existing = CredentialsProvider.lookupCredentials(
    StringCredentialsImpl.class, jenkins, null, null
).find { it.id == 'sonar-token' }

if (!existing) {
def sonarCred = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    'sonar-token',
    'Token de SonarQube generado desde UI',
    Secret.fromString('squ_a92554756dca8a15f2e71112c827958c73bd0835')
)
credStore.addCredentials(domain, sonarCred)
println "Credencial 'sonar-token' creada"
}

// ─────────────────────────────────────────────
// 5. Deshabilitar CSRF para facilitar ejecucion via API
// ─────────────────────────────────────────────
// Deshabilitar CSRF - metodo correcto
try {
    def crumbIssuer = jenkins.getCrumbIssuer()
    if (crumbIssuer != null) {
    jenkins.setCrumbIssuer(null)
        println "CSRF deshabilitado correctamente"
    } else {
        println "CSRF ya estaba deshabilitado"
}
} catch (Exception e) {
    println "No se pudo deshabilitar CSRF: ${e.message}"
}

jenkins.save()
println "Inicialización de Jenkins completada"
