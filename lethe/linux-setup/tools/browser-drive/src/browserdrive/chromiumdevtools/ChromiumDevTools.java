package browserdrive.chromiumdevtools;

import static browserdrive.Main.driver;
import static browserdrive.Main.wait;

import java.io.PrintStream;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromiumDevTools {

	public static final String nameArg = "chromiumdevtools";

	static Args arguments;

	public static void printUsage(PrintStream s) {
		Args.printHelp(s);
	}

	private static void openDevTools() {
		// Found this through chrome://chrome-urls/
		driver.get("chrome://inspect/#pages");
		try {
			// this works in Chromium v33 (in v32 the structure is different)
			String selector = "div#pages-list div.actions span.action";
			driver.findElement(By.cssSelector(selector)).click();
		} catch (Exception e) {
			System.err.println("Caught an exception, but this is expected");
		}
		try {
			// Sleep so the sqlite3 file gets data put into it
			// Choosing a higher number to also work on slower systems,
			// or ‘cold boot’ ones.
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		driver.quit();
	}

	public static void run(String[] args) {
		arguments = Args.getArgsOrExit(args);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--user-data-dir=" + arguments.user_data_dir);
		options.addArguments("about:blank");
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, 5);

		openDevTools();
	}

}
