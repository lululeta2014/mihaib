package browserdrive.chromium;

import static browserdrive.Main.click;
import static browserdrive.Main.clickX;
import static browserdrive.Main.clickXFirstFound;
import static browserdrive.Main.delay;
import static browserdrive.Main.driver;
import static browserdrive.Main.homepage;
import static browserdrive.Main.switchToFrameX;
import static browserdrive.Main.tick;
import static browserdrive.Main.tickX;
import static browserdrive.Main.untick;
import static browserdrive.Main.waitForInvisibility;
import static browserdrive.Main.waitForVisibility;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

class Version28 {

	static void run() {
		driver.get("chrome://chrome/settings/");
		switchToFrameX("//iframe[@src='chrome://settings-frame/']");

		// Set startup page
		click("startup-show-pages");
		click("startup-set-pages");
		waitForVisibility("startup-overlay");
		WebElement pageList = driver.findElement(By.id("startupPagesList"));
		WebElement addrField = pageList.findElement(By
				.xpath("//input[@type='text' and @class='weakrtl']"));
		addrField.clear();
		// on a fast computer, without "\n" or moving the cursor away before
		// clicking "ok", it behaves as if you entered nothing in the textbox
		// and clicked ok: and the radio button jumps back to
		// "Open the New Tab page".
		addrField.sendKeys(homepage + "\n");
		click("startup-overlay-confirm");
		waitForInvisibility("startup-overlay");

		// Use Classic Theme
		click("themes-reset");

		// Set homepage
		tick("show-home-button");
		waitForVisibility("change-home-page-section");
		click("change-home-page");
		waitForVisibility("home-page-overlay");
		click("homepage-use-url");
		WebElement homepageField = driver.findElement(By
				.id("homepage-url-field"));
		homepageField.clear();
		homepageField.sendKeys(homepage);
		click("home-page-confirm");
		waitForInvisibility("home-page-overlay");

		untick("show-bookmark-bars");
		untick("show-window-decorations"); // system title bar and borders

		untick("instant-enabled-control"); // use instant for faster searching

		// Show advanced settings
		click("advanced-settings-expander");
		waitForVisibility("advanced-settings");

		// Privacy

		// Privacy.Content Settings
		click("privacyContentSettingsButton");
		waitForVisibility("content-settings-page");
		clickX("//input[@type='radio' and @name='cookies' and @value='session']");
		tickX("//input[@pref='profile.block_third_party_cookies']");

		click("show-cookies-button");
		waitForVisibility("cookies-view-page");
		clickX("//button[@class='remove-all-cookies-button']");
		clickX("//button[@class='cookies-view-overlay-confirm']");
		waitForInvisibility("cookies-view-page");
		clickX("//input[@type='radio' and @name='handlers' and @value='block']");
		clickX("//input[@type='radio' and @name='location' and @value='block']");
		clickX("//input[@type='radio' and @name='notifications' and @value='block']");
		clickX("//input[@type='radio' and @name='mouselock' and @value='ask']");
		clickX("//input[@type='radio' and @name='media-stream' and @value='block']");
		// Unsandboxed plug-in access
		clickX("//input[@type='radio' and @name='ppapi-broker' and @value='block']");
		click("content-settings-overlay-confirm");
		waitForInvisibility("content-settings-page");

		// Privacy.Clear Browsing Data
		click("privacyClearDataButton");
		waitForVisibility("clear-browser-data-overlay");
		clickX("//select[@id='clear-browser-data-time-period']/option[@value='4']");
		tick("delete-browsing-history-checkbox");
		tick("delete-download-history-checkbox");
		tick("delete-cookies-checkbox");
		tick("delete-cache-checkbox");
		tick("delete-form-data-checkbox");
		tick("delete-hosted-apps-data-checkbox");
		click("clear-browser-data-commit");
		waitForInvisibility("clear-browser-data-overlay");

		// Privacy
		untick("alternateErrorPagesEnabled"); // prediction, navigation
		untick("searchSuggestEnabled");
		untick("dnsPrefetchingEnabled");
		tick("do-not-track-enabled");
		waitForVisibility("do-not-track-confirm-overlay");
		click("do-not-track-confirm-ok");
		waitForInvisibility("do-not-track-confirm-overlay");

		// present in Ubuntu 13.04, missing in Debian Jessie
		try {
			untick("promptIntegrationForAnyWebsite");
		} catch (NoSuchElementException e) {
		}

		// Passwords and Forms
		untick("autofill-enabled");
		if (Chromium.arguments.save_passwords)
			tick("password-manager-enabled");
		else
			untick("password-manager-enabled");

		// Web Content
		click("fontSettingsCustomizeFontsButton");
		waitForVisibility("font-settings");
		clickXFirstFound(new String[] {
				"//select[@id='standard-font-family']/option[@value='DejaVu Serif']",
				"//select[@id='standard-font-family']/option[@value='Times New Roman']" });
		clickXFirstFound(new String[] {
				"//select[@id='serif-font-family']/option[@value='DejaVu Serif']",
				"//select[@id='serif-font-family']/option[@value='Times New Roman']" });
		clickXFirstFound(new String[] {
				"//select[@id='sans-serif-font-family']/option[@value='DejaVu Sans']",
				"//select[@id='sans-serif-font-family']/option[@value='Arial']" });
		clickXFirstFound(new String[] {
				"//select[@id='fixed-font-family']/option[@value='DejaVu Sans Mono']",
				"//select[@id='fixed-font-family']/option[@value='Monospace']" });

		clickX("//select[@id='font-encoding']/option[@value='UTF-8']");
		delay(1000); // or UTF-8 encoding won't be saved

		click("font-settings-confirm");
		waitForInvisibility("font-settings");

		// Languages
		if (Chromium.arguments.enable_translate)
			tick("enableTranslate");
		else
			untick("enableTranslate");

		// Downloads
		// If we want to "Ask where to save each file before downloading"
		// tickX("//input[@type='checkbox' and @pref='download.prompt_for_download']");

		// System
		untick("backgroundModeCheckbox");

		// Currently the browser sees this as as unclean shutdown and the last
		// settings we change aren't saved.
		delay(3000);

		driver.quit();
	}
}
