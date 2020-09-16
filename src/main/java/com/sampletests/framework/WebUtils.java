package com.sampletests.framework;

import cucumber.api.Scenario;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class WebUtils {
    private DriverUtil driverUtil;
    private WebDriver driver;
    private PropertyUtil propertyUtil;
    private Properties props;
    private WebDriverWait wait;
    private int maxWaitinSeconds = 0;
    private HashMap<String, Date> startTimes = new HashMap();
    private Scenario scenario;
    private String envName;
    String userdir = System.getProperty("user.dir");
    public static String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);
    private static final String TASKLIST = "tasklist";
    private static String KILL = "\\System32\\taskkill /F /IM ";

    public WebDriverWait waitFor() {
        return wait;
    }

    public WebUtils() throws Throwable {
        propertyUtil = new PropertyUtil();
        envName = System.getProperty("env");
        LOGGER.info("Executing tests on: " + envName + " environment.");
        loadProperties(envName);
        checkforPropertiesOverride();
        driverUtil = new DriverUtil(props);
        String maxWaitTime = getValueFromProperties("MaxWaitTime");
        if (maxWaitTime == null) {
            maxWaitinSeconds = 120;
        } else {
            maxWaitinSeconds = Integer.parseInt(maxWaitTime);
        }
        if (getDriver() != null) {
            wait = new WebDriverWait(getDriver(), maxWaitinSeconds);
            PageFactory.initElements(getDriver(), this);
        } else {
            throw new Exception("Web Driver set incorrectly. Stopping the execution.");
        }
    }

    private boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    private boolean isLinux() {
        return (OS.indexOf("linux") >= 0);
    }

    public void loadProperties(String envName) throws Throwable {
        props = propertyUtil.getPropertiesForEnvironment(envName);
        LOGGER.info("Property values: " + props);
    }

    private void checkforPropertiesOverride() {
        Set<Object> allProperties = props.keySet();

        for (Object k : allProperties) {
            String key = (String) k;
            if (System.getProperty(key) != null) {
                props.setProperty(key, System.getProperty(key));
                LOGGER.info("Set property: " + key + " as: " + System.getProperty(key));
            }
        }
    }

    public String getValueFromProperties(String key) {
        return props.getProperty(key);
    }

    public WebDriver getDriver() {
        if (driver == null) {
            driver = driverUtil.getDriver();
            if (driver == null) {
                LOGGER.info("Webdriver is not set correctly hence closing the test execution.");
            } else {
                LOGGER.info("In side getDriver and driver is: " + driver);
            }
            // getDriverNameAndVersion();
        }
        return driver;
    }

    /*
     * @param String servicename
     *
     * @Description This function will kill the service passed to it
     */

    public static void killProcess(String serviceName) throws Exception {
        KILL = System.getenv("SystemRoot") + KILL;
        Runtime.getRuntime().exec(KILL + serviceName);
    }

    public void takeScreenShot(String screenshotDir, String fileName) {
        try {
            byte[] screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
            File file = new File(screenshotDir + fileName);
            int i = 0;
            while (file.exists()) {
                file = new File(screenshotDir + fileName + "-" + ".png");
            }
            FileUtils.writeByteArrayToFile(file, screenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *
     * @param String servicename
     *
     * @Description This function will check if servicename passed to it is running
     * on the system and accordingly return boolean value
     */

    public boolean isProcessRunning(String serviceName) throws Exception {
        if ((!isMac()) && (!isLinux())) {
            Process p = Runtime.getRuntime().exec(TASKLIST);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                if (line.contains(serviceName)) {
                    return true;
                }
            }

        }
        return false;
    }

    public void closeBrowser(){
        driverUtil.closeDriver();
    }

    public WebElement getElementUsingID(String elementId) {
        WebElement ele = null;
        try {
            if (elementId != null) {
                ele = getDriver().findElement(By.id(elementId));
            }

        } catch (Exception ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("getElementUsingID() - Element not found using element id: " + elementId);
            }
        }

        return ele;
    }

    public void killDrivers() throws Throwable {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("killDrivers() - Killing the drivers after test execution");
        }

        if ((!isMac()) && (!isLinux())) {
            if (isProcessRunning("IEDriverServer.exe")) {
                killProcess("IEDriverServer.exe");
            } else if (isProcessRunning("chromedriver.exe")) {
                killProcess("chromedriver.exe");
            } else {
                killProcess("geckodriver.exe");
            }
        }
    }

    public void visitPage(String newUrl) {
        if (getDriver() != null) {
            driver.get(newUrl);
            LOGGER.info("visitPage() - Visiting page: " + newUrl);

            // Overide ssl certificate issue
            WebElement errorMessage = getElementUsingID("overridelink");
            if (errorMessage != null) {
                errorMessage.click();
                LOGGER.info("Certificate issue found.");
            }
        }
    }

    public void clickUsinfActionClass(WebElement elemetToBeClicked) {
        Actions action = new Actions(driver);
        action.moveToElement(elemetToBeClicked).click().build().perform();
    }

    public void clickJavaScriptExec(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void refreshPage() {
        driver.navigate().refresh();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    public void selectVisibleTextFromAList(WebElement selectElement, String visibleText) {
        Select select = new Select(selectElement);
        select.selectByVisibleText(visibleText);
    }

    public void selectByIndexFromAList(WebElement selectElement, int indexValue) {
        Select select = new Select(selectElement);
        select.selectByIndex(indexValue);
    }

    public boolean verifyExpectedText(WebElement element, String expectedText) {
        boolean textMatch = false;
        waitForElementToBeVisisble(element);
        if (element.getText().equals(expectedText)) {
            textMatch = true;
            LOGGER.info("Actual text diaplyed: " + element.getText());
        } else {
            LOGGER.info("Text doesnt match or dispaled");
            LOGGER.info("Actual msg: " + element.getText() + "\n");
            LOGGER.info("Expectted msg" + expectedText);
        }
        return textMatch;
    }

    public boolean verifyExpectedTextIsContained(WebElement element, String expectedText) {
        boolean textContained = false;
        waitForElementToBeVisisble(element);
        if (element.getText().contains(expectedText)) {
            textContained = true;
            //LOGGER.info("Actual text diaplyed: " + element.getText());
        } else {
            LOGGER.info("Text does not contain expected message");
            LOGGER.info("Actual msg: " + element.getText() + "\n");
            LOGGER.info("Expectted msg" + expectedText);
        }
        return textContained;
    }

    public void waitForElementToBeVisisble(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForElementToBeClickable(WebElement elementToLoad) {
        wait.until(ExpectedConditions.elementToBeClickable(elementToLoad));
        return elementToLoad;
    }

    public void waitForSeconds(int seconds) {
        driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }


    public void setScenario(Scenario currentScenario) {
        this.scenario = currentScenario;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("setScenario() - Scenario Name: " + scenario.getName());
        }
    }

    public void setStartTimeForScenario(String scenarioName, Date startTime) {
        startTimes.put(scenarioName, startTime);
    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public String getCurrentDatePlusThirtyDays(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        String futureDate = dateFormat.format(c.getTime());
        LOGGER.info("Current date plus one month:--------> " + futureDate);
        return futureDate;
    }

    public String getCurrentDateMinusAMonth(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        String iwDate = dateFormat.format(c.getTime());
        LOGGER.info("Purchase date:--------> " + iwDate);
        return iwDate;
    }

    public String getDateOOWappliance(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -3);
        String oowDate = dateFormat.format(c.getTime());
        LOGGER.info("Purchase date:--------> " + oowDate);
        return oowDate;
    }

    public String getCurrentDateMinusAnYearAndADay(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        c.add(Calendar.DATE, -1);
        String iwDate = dateFormat.format(c.getTime());
        LOGGER.info("Purchase date:--------> " + iwDate);
        return iwDate;
    }



    public String getFutureDateYear(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        c.add(Calendar.YEAR, 1);
        String futureDate = dateFormat.format(c.getTime());
        LOGGER.info("Current date plus one year:--------> " + futureDate);
        return futureDate;
    }

    public String getCurrentDatePlusYear(String dateForm) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dateForm);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        String futureDate = dateFormat.format(c.getTime());
        LOGGER.info("Current date plus one year:--------> " + futureDate);
        return futureDate;
    }

    public boolean pageContainsText(String text) {
        return driver.getPageSource().contains(text);
    }

    public boolean isElemetContainsText(WebElement element, String text){
        if(element.getText().contains(text)){
            return  true;
        }else return false;
    }

    public String getRandomAlphaString(int len) {
       // String randString = generateRandomString(len);
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
        //return randString;
    }

    public boolean isElementPresent(List<WebElement> element) {
        boolean present = false;
        try {
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            if (element.size() != 0) {
                present = true;
            }
        } catch (NoSuchElementException e) {
            present = false;
        } finally {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
        return present;
    }

    public void fillInTextField(WebElement ele, String text){
        ele.clear();
        ele.sendKeys(text);
    }

    public boolean isElementFound(String locatorTpye, String locator) {
        boolean isElementFound = false;
        WebElement ele = null;
        try {
            switch (locatorTpye.toLowerCase()) {

                case "id":
                    ele = driver.findElement(By.id(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "css":
                    ele = driver.findElement(By.cssSelector(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "link":
                    ele = driver.findElement(By.linkText(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "className":
                    ele = driver.findElement(By.className(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "name":
                    ele = driver.findElement(By.name(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "tagName":
                    ele = driver.findElement(By.tagName(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

                case "xpath":
                    ele = driver.findElement(By.xpath(locator));
                    if (ele != null) {
                        isElementFound = true;
                    }
                    break;

            }
        } catch (Exception e) {
            isElementFound = false;
        }

        return isElementFound;
    }

    public String generateRandomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public boolean existsElement(By by) {
        boolean present = false;
        try {
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            List<WebElement> element = driver.findElements(by);
            if (element.size() != 0) {
                present = true;
            }
        } catch (NoSuchElementException e) {
            present = false;
            LOGGER.info("Elemet is not present" + by);
        } finally {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
        return present;
    }

    public boolean existsSubElement(WebElement element, By by) {
        boolean present = false;
        try {
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            List<WebElement> elements = element.findElements(by);
            if (elements.size() != 0) {
                present = true;
            }
        } catch (NoSuchElementException e) {
            present = false;
            LOGGER.info("Elemet is not present" + by);
        } finally {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
        return present;
    }

    public void clickOnlink(String linkText) {
        clickJavaScriptExec(driver.findElement(By.linkText(linkText)));
        LOGGER.info("Clicked on link:" + linkText);
    }

    public void scrollTOView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void clickOnLinkUsingID(String id) {
        driver.findElement(By.id(id)).click();
        LOGGER.info("Clicked on link---->" + id);
        waitForSeconds(3);
    }

    public boolean veifyTextAgaistList(String expectedText, List<WebElement> actualText) {
        boolean isTextPresent = false;
        for (WebElement text : actualText) {
            if (text.getText().contains(expectedText)) {
                isTextPresent = true;
                LOGGER.info("Expected text is displayed" + text.getText());
                break;
            } else {

            }
        }
        if (!isTextPresent) {
            LOGGER.info("Expected text is not displayed: " + expectedText);
            LOGGER.info("isTextPresent Value print" + isTextPresent);
        }
        return isTextPresent;
    }

    public void waiForElementToBeEnebledAndClick(WebElement ele) {
        wait.until(ExpectedConditions.elementToBeClickable(ele)).click();

    }

    public void clickOnSubmitButton() {
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        waitForSeconds(2);
    }

    public void selectValueFromAList(WebElement selectElement, String value) {
        Select select = new Select(selectElement);
        select.selectByValue(value);
    }
}
