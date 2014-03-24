package browserdrive;

import java.nio.file.Paths;
import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import browserdrive.chromium.Chromium;
import browserdrive.chromiumdevtools.ChromiumDevTools;
import browserdrive.chromiumgmail.ChromiumGmail;
import browserdrive.firefox.Firefox;
import browserdrive.firefoxgmail.FirefoxGmail;

public class Main {

	public static WebDriver driver;
	public static WebDriverWait wait;
	public static final String homepage = "https://encrypted.google.com/";
	public static final String desktop = Paths.get(
			System.getProperty("user.home"), "Desktop").toString();

	private static final boolean delay = false;
	private static final long delayMillis = 2000;

	private static void delay() {
		if (delay) {
			delay(delayMillis);
		}
	}

	public static void delay(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	public static void click(String id) {
		delay();
		WebElement elem = driver.findElement(By.id(id));
		elem.click();
	}

	public static void tick(String id) {
		delay();
		WebElement elem = driver.findElement(By.id(id));
		if (!elem.isSelected())
			elem.click();
	}

	public static void untick(String id) {
		delay();
		WebElement elem = driver.findElement(By.id(id));
		if (elem.isSelected())
			elem.click();
	}

	public static void clickX(String xpath) {
		delay();
		WebElement elem = driver.findElement(By.xpath(xpath));
		elem.click();
	}

	public static void tickX(String xpath) {
		delay();
		WebElement elem = driver.findElement(By.xpath(xpath));
		if (!elem.isSelected())
			elem.click();
	}

	public static void untickX(String xpath) {
		delay();
		WebElement elem = driver.findElement(By.xpath(xpath));
		if (elem.isSelected())
			elem.click();
	}

	/**
	 * Call clickX() on each array item, stop at the first call which doesn't
	 * throw NoSuchElementException or when clickX() has been called for all
	 * items in the array.
	 */
	public static void clickXFirstFound(String[] xpaths) {
		for (String xpath : xpaths) {
			try {
				clickX(xpath);
				return;
			} catch (NoSuchElementException e) {
			}
		}
	}

	public static void switchToFrameX(String xpath) {
		driver.switchTo().frame(driver.findElement(By.xpath(xpath)));

	}

	public static void waitForInvisibility(By by) {
		wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
	}

	public static void waitForInvisibility(String id) {
		waitForInvisibility(By.id(id));
	}

	public static void waitForVisibility(By by) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}

	public static void waitForVisibility(String id) {
		waitForVisibility(By.id(id));
	}

	public static void printUsageAndExit(int exitcode) {
		System.out.println("Usage: browser-drive ARGS. ARGS are:");

		System.out.println();
		System.out.print(Chromium.nameArg + " ");
		Chromium.printUsage(System.out);

		System.out.println();
		System.out.print(ChromiumDevTools.nameArg + " ");
		ChromiumDevTools.printUsage(System.out);

		System.out.println();
		System.out.print(ChromiumGmail.nameArg + " ");
		ChromiumGmail.printUsage(System.out);

		System.out.println();
		System.out.print(Firefox.nameArg + " ");
		Firefox.printUsage(System.out);

		System.out.println();
		System.out.print(FirefoxGmail.nameArg + " ");
		FirefoxGmail.printUsage(System.out);

		System.exit(exitcode);
	}

	public static void main(String[] args) {
		if (args.length == 0)
			printUsageAndExit(1);

		String first = args[0];
		String[] rest = Arrays.copyOfRange(args, 1, args.length);

		switch (first) {
		case Chromium.nameArg:
			Chromium.run(rest);
			break;
		case ChromiumDevTools.nameArg:
			ChromiumDevTools.run(rest);
			break;
		case ChromiumGmail.nameArg:
			ChromiumGmail.run(rest);
			break;
		case Firefox.nameArg:
			Firefox.run(rest);
			break;
		case FirefoxGmail.nameArg:
			FirefoxGmail.run(rest);
			break;
		default:
			printUsageAndExit(1);
		}
	}
}
