package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
/**
 * Runner para ejecutar las pruebas de extremo a extremo
 * con Cucumber y JUnit
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "support"},
    plugin = {"pretty", "html:target/cucumber-reports/report.html"},
    monochrome = true,
    publish = true,
    snippets = CucumberOptions.SnippetType.CAMELCASE,
    tags = "not @ignore"
)
public class TestRunner {
}