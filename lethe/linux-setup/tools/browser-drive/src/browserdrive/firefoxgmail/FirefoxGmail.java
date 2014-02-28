package browserdrive.firefoxgmail;

import static browserdrive.Main.click;
import static browserdrive.Main.driver;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

public class FirefoxGmail {

	public static final String nameArg = "firefox-gmail";

	public static void printUsage(PrintStream s) {
		Args.printHelp(s);
	}

	public static void run(String[] args) {
		Args arguments = Args.getArgsOrExit(args);

		FirefoxProfile profile = new ProfilesIni()
				.getProfile(arguments.profile_name);
		driver = new FirefoxDriver(profile);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		driver.get("https://mail.google.com/");

		try {
			// If we hit a fancy landing page
			driver.findElement(By.id("gmail-sign-in")).click();
		} catch (NoSuchElementException e) {
		}

		WebElement email = driver.findElement(By.id("Email"));
		// Just in case saved passwords pre-fill the fields
		email.clear();
		email.sendKeys(arguments.username);

		WebElement passwd = driver.findElement(By.id("Passwd"));
		passwd.click();
		if (arguments.password != null) {
			passwd.clear();
			passwd.sendKeys(arguments.password);
		}
		click("signIn");

		// Falling off the end here works well in Firefox:
		// The Java process waits for the user to close the browser.
	}

}
