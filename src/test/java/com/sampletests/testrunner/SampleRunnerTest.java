package com.sampletests.testrunner;

import com.github.mkolisnyk.cucumber.runner.ExtendedCucumber;
import com.github.mkolisnyk.cucumber.runner.ExtendedCucumberOptions;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(ExtendedCucumber.class)
@ExtendedCucumberOptions(jsonReport = "target/cucumber.json",
        retryCount = 0,
        detailedReport = true,
        detailedAggregatedReport = true,
        overviewReport = true,
        coverageReport = false,
        //jsonUsageReport = "target/cucumber-usage.json",
        usageReport = true,
        toPDF = true,
        includeCoverageTags = {"@passed" },
        outputFolder = "target/")
@CucumberOptions(plugin = { "json:target/cucumber.json", "pretty:target/cucumber-pretty.txt",
        "usage:target/cucumber-usage.json", "junit:target/cucumber-results.xml"},
        features="classpath:features",
        glue = "com.domgen.automation",
        tags={"@regression, @uat"}

)

public class SampleRunnerTest {

}
