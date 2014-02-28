package browserdrive.chromiumgmail;

import static browserdrive.Main.click;
import static browserdrive.Main.driver;

import java.io.PrintStream;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromiumGmail {

	public static final String nameArg = "chromium-gmail";

	private static final String password_store = "gnome";

	public static void printUsage(PrintStream s) {
		Args.printHelp(s);
	}

	public static void run(String[] args) {
		Args arguments = Args.getArgsOrExit(args);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--user-data-dir=" + arguments.user_data_dir);
		options.addArguments("--password-store=" + password_store);
		if (arguments.incognito)
			options.addArguments("--incognito");
		driver = new ChromeDriver(options);

		driver.get("https://mail.google.com/");

		try {
			// If we hit a fancy landing page
			driver.findElement(By.id("gmail-sign-in")).click();
		} catch (NoSuchElementException e) {
		}

		WebElement email = driver.findElement(By.id("Email"));
		// If not incognito and with saved passwords, the fields are pre-filled
		email.clear();
		email.sendKeys(arguments.username);
		click("Passwd");
		click("signIn");

		// Falling off the end here works well in Firefox:
		// The Java process waits for the user to close the browser.
		// This doesn't work with Chromium: when the user closes the browser, an
		// error is printed; the chromedriver & java processes hang forever.

		// Exit the Java process here with System.exit() and ‘killall
		// chromedriver’ from the .sh script whenever you run ChromiumGmail, to
		// clean up previous hanging instances.
		System.exit(0);
	}

}
