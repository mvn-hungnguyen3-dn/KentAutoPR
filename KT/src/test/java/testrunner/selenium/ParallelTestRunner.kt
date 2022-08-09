package testrunner.selenium

import io.cucumber.testng.CucumberOptions
import kent.core.ParallelCucumberRunnerBase

@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["stepdefs"],
    tags = ("not @Ignore"),
    plugin = ["pretty",
        "junit:target/cucumber-reports/junit-report.xml",
        "html:target/cucumber-reports/cucumber-pretty",
        "json:target/cucumber-reports/CucumberTestReport.json",
        "rerun:target/cucumber-reports/rerun.txt"]
)
internal class ParallelTestRunner : ParallelCucumberRunnerBase()
