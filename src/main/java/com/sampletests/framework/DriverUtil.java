package com.sampletests.framework;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by remyagoldie on 08/04/2019.
 *
 * This class manages the driver instance
 */
public class DriverUtil {

	private static WebDriver driver;
	private Properties props;
	private String userPath = System.getProperty("user.dir");
	private String osName = System.getProperty("os.name");
	private static final Logger LOGGER = LoggerFactory.getLogger(DriverUtil.class);

	public DriverUtil(Properties envProps) {
		props = envProps;
	}

	public void setDriver() {
		DesiredCapabilities capabilities = null;
		LOGGER.info("Inside set Driver");
		if (driver == null) {
			// check for os
			if (osName.toLowerCase().contains("windows")) {
				LOGGER.info("We are running on Windows Machine and on browser: " + props.getProperty("browser"));
				LOGGER.info("User Path: " + userPath);
				ChromeOptions options;
				switch (props.getProperty("browser").toLowerCase()) {
					case "firefox":
						System.setProperty("webdriver.gecko.driver",
								userPath + "/src/test/resources/drivers/geckodriver.exe");
						driver = new FirefoxDriver();
						LOGGER.info("Firefox driver initiated: " + driver);
						break;

					case "ie":
						System.setProperty("webdriver.ie.driver",
								userPath + "/src/test/resources/drivers/IEDriverServer.exe");
						driver = new InternetExplorerDriver();
						break;

					case "chrome":
						System.setProperty("webdriver.chrome.driver",
								userPath + "/src/test/resources/drivers/chromedriver.exe");
						driver = new ChromeDriver();
						break;

					case "jenkins":
						System.setProperty("webdriver.chrome.driver",
								"C:/Program Files (x86)/Jenkins/tools/chromedriver/chromedriver.exe");
						options = new ChromeOptions();
						driver = new EventFiringWebDriver(new ChromeDriver(options));
						break;

					case "headless":
						System.setProperty("webdriver.chrome.driver",
								userPath + "/src/test/resources/drivers/chromedriver.exe");

						options = new ChromeOptions();
						options.addArguments("headless");
						options.addArguments("window-size=1400x800");
						driver = new ChromeDriver(options);
						break;
				}
			}else {
				LOGGER.info("Setting the default driver to be Headless Chrome using "+osName.toLowerCase()+" OS default binary and driver");
				ChromeOptions options = new ChromeOptions();
				options.addArguments("no-sandbox");
				options.addArguments("headless");
				options.addArguments("disable-extensions");
				options.addArguments("disable-dev-shm-usage");
				options.addArguments("start-maximized");
				options.addArguments("window-size=1400,800");
				driver = new ChromeDriver(options);
			}
			if (driver != null) {
				driver.manage().window().maximize();
			}
		}

		LOGGER.info("Set driver: " + driver);
	}

	public WebDriver getDriver() {
		// Setting Web driver
		setDriver();

		// Setting SQL driver
		setSqlDriver();
		return driver;
	}

	public void closeDriver() {
		driver.quit();
	}

	public void setSqlDriver() {
		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver for DB2");
			LOGGER.info("**** Loaded the JDBC driver for DB2");

			Class.forName("com.ibm.as400.access.AS400JDBCDriver");
			System.out.println("**** Loaded the JDBC driver for AS400");
			LOGGER.info("**** Loaded the JDBC driver for AS400");
		}

		catch (ClassNotFoundException e) {
			LOGGER.warn("Could not load JDBC driver");
			System.err.println("Could not load JDBC driver");
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}

}
