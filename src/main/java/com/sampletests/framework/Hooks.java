package com.sampletests.framework;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Hooks {
    private WebUtils utils;
    private static int passedTestCount;
    private static int failedTestCount;
    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);


    private static boolean initialized = false;
    private static final SimpleDateFormat SCREENSHOT_FILENAME_DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMdd'-'HHmmssSSS");

    public Hooks(WebUtils webUtils) {
        utils = webUtils;
    }

    @Before(order = 1)
    public void allowConnectionToBadlySetupServers() {
        // turns off unrecognised SSL warning which stops connection to badly
        // setup servers
       // System.setProperty("jsse.enableSNIExtension", "false");
        utils.getDriver().manage().deleteAllCookies();
    }

    @Before(order = 2)
    public void before(Scenario scenario)
    {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Executing scenario -->" + scenario.getName() + "\n");
        }
        utils.setScenario(scenario);
        utils.setStartTimeForScenario(scenario.getName(),  new Date(System.currentTimeMillis()));

    }

    /**
     *
     * @param : Scenario scenario
     * @return : void
     * @Description :This function will print the scenario status at the end of
     *              test execution
     *
     * */

    @After(order = 4)
    public void printScenarioStatus(Scenario scenario) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(" Printing Scenario status below" + "\n");
            LOGGER.info("Hooks() - printScenarioStatus() order=3"
                    + "Scenario with name  -->" + scenario.getName()
                    + " --> has been executed and its status is -->"
                    + scenario.getStatus() + "\n");
            if (scenario.getStatus().equalsIgnoreCase("passed")) {
                passedTestCount = passedTestCount + 1;
            } else if (scenario.getStatus().equalsIgnoreCase("failed")) {
                failedTestCount = failedTestCount + 1;
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("PASSED TEST CASE COUNT -->" + passedTestCount
                    + "  FAILED TEST CASE COUNT -->" + failedTestCount + "\n");
        }
    }



    /**
     *
     * @param : Scenario scenario
     * @return : void
     * @Description :This function will delete the cookies at the end of test
     *              execution
     *
     * */
    @After(order = 3)
    public void deleteCookiesAfterTestExecution()
    {

        //LOGGER.info("deleteCookiesAfterTestExecution() -order=3, Delete all cookies at the end of the test execution");
        //PropertyUtil propUtil = PropertyUtil.getInstance();
        if (utils.getDriver() != null)
        {
            utils.getDriver().manage().deleteAllCookies();
            //LOGGER.info("deleteCookiesAfterTestExecution() Deleted all cookies at the end of the test execution");
        }
    }


    @Before
    public void setUp() throws Throwable {
        if (!initialized) {
            // Init context. Run just once before first scenario starts

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {

                    if (utils.getDriver() != null) {
                        try {

                            utils.getDriver().quit();
//                            utils.killDrivers();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    } else {
                        LOGGER.info("Driver instance was null in End of test. Nothing to kill");
                    }
                }
            });
        }

    }

    /**
     *
     * @param : Scenario scenario
     * @return : void
     * @Description :This function take screenshot if the scenario fails
     *
     *
     * */
    @After(order = 5)
    public void captureScreenshot(Scenario scenario)
    {
        if (scenario.isFailed() && utils.getDriver() != null)
        {
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("captureScreenshot() - order=4 - capturing the screenshot for the failed test");
            }
            String workingDir = System.getProperty("user.dir");
            String screenshotDir = workingDir
                    + "//target//selenium-test-screenshots//";
            String fileName = (scenario.getName() != null ? ""
                    + scenario.getName().replace(' ', '-') : "")
                    + "_"
                    + SCREENSHOT_FILENAME_DATE_FORMAT.format(new Date())
                    + ".png";
            utils.takeScreenShot(screenshotDir, fileName);
        }
    }
}
