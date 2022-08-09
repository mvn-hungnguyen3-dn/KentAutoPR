package testrunner.selenium

import io.cucumber.testng.CucumberOptions
import kent.core.CucumberRunnerBase

@CucumberOptions(
    tags = ("@HealthCheckWeb"),
    features = ["src/test/resources/features"],
    glue = ["stepdefs"],
    plugin = ["pretty", "timeline:target/cucumber-reports/timeline", "junit:target/cucumber-reports/junit-report.xml", "html:target/cucumber-reports/cucumber-pretty.html", "json:target/cucumber-reports/CucumberTestReport.json", "rerun:target/cucumber-reports/rerun.txt"],
    monochrome = true,
    strict = true
)
internal class DefaultTestRunner : CucumberRunnerBase()
