import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition

def jenkins = Jenkins.get()
def job = jenkins.getItem("empleados-service-ci")
if (job != null) {
    def file = new File("/code/empleados-service/Jenkinsfile")
    def pipelineScript = file.text
    job.definition = new CpsFlowDefinition(pipelineScript, true)
    job.save()
    println "OK: Job actualizado"
} else {
    println "ERROR: Job no encontrado"
}
