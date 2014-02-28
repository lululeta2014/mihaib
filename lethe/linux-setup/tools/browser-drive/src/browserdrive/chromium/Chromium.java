package browserdrive.chromium;

import static browserdrive.Main.driver;
import static browserdrive.Main.wait;

import java.io.PrintStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Chromium {

	public static final String nameArg = "chromium";

	private static final String password_store = "gnome";

	static Args arguments;

	public static void printUsage(PrintStream s) {
		Args.printHelp(s);
	}

	private static void dispatchByVersion() {
		driver.get("chrome://version");
		WebElement versionSpan = driver.findElement(By
				.xpath("//span[@i18n-content='version']"));
		try {
			String fullVersion = versionSpan.getText().trim();
			int idx = fullVersion.indexOf('.');
			if (idx > 0) {
				String majVersion = fullVersion.substring(0, idx);
				int version = Integer.parseInt(majVersion);
				if (version <= 18) {
					Version18.run();
				} else if (version < 21) {
					Version20.run();
				} else if (version < 22) {
					Version21.run();
				} else if (version < 24) {
					Version22.run();
				} else if (version < 25) {
					Version24.run();
				} else if (version < 28) {
					Version25.run();
				} else if (version < 29) {
					Version28.run();
				} else {
					runLatestVersion();
				}
				return;
			}
		} catch (NumberFormatException e) {
		}
		runLatestVersion();
	}

	private static void runLatestVersion() {
		Version29.run();
	}

	public static void run(String[] args) {
		arguments = Args.getArgsOrExit(args);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--user-data-dir=" + arguments.user_data_dir);
		options.addArguments("--password-store=" + password_store);
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, 5);

		dispatchByVersion();
	}

}
