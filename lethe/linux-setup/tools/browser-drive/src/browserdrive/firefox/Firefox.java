package browserdrive.firefox;

import static browserdrive.Main.driver;
import static browserdrive.Main.printUsageAndExit;

import java.io.File;
import java.io.PrintStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class Firefox {

	public static final String nameArg = "firefox";

	public static void printUsage(PrintStream s) {
		s.println("user_data_dir");
		s.println("    Just confirms Adblock Plus installation");
		s.println("    DOES NOT WORK (FF.10 on Wheezy or FF15 on Ubuntu)");
	}

	/**
	 * Fragile as an egg.
	 * 
	 * Needs Native Events, currently doesn't work for Firefox 10 in Debian
	 * Wheezy.
	 */
	public static void run(String[] args) {
		if (args.length != 1)
			printUsageAndExit(1);

		String profile_dir = args[0];
		FirefoxProfile profile = new FirefoxProfile(new File(profile_dir));
		profile.setEnableNativeEvents(true);
		driver = new FirefoxDriver(profile);

		driver.get("about:");
		WebElement versionElem = driver.findElement(By.id("version"));
		System.out.println(versionElem.getText());
		// "version 15.0"

		// We need multiple tabs so that Firefox doesn't close when we click
		// Continue and this tab closes
		versionElem.sendKeys(Keys.CONTROL + "t");

		driver.get("about:newaddon?id={d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}");

		driver.findElement(By.id("allow")).sendKeys(" ");
		driver.findElement(By.id("continue-button")).sendKeys(" ");
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
		}
		driver.quit();
	}

}
