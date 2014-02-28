package browserdrive.chromium;

import static browserdrive.Main.click;
import static browserdrive.Main.clickX;
import static browserdrive.Main.clickXFirstFound;
import static browserdrive.Main.driver;
import static browserdrive.Main.homepage;
import static browserdrive.Main.tick;
import static browserdrive.Main.tickX;
import static browserdrive.Main.untick;
import static browserdrive.Main.waitForInvisibility;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class Version18 {

	static void run() {
		// 1. Basics
		driver.get("chrome://settings");

		clickX("//input[@type='radio' and @name='startup' and @pref='session.restore_on_startup' and @value='0']");
		click("homepageUseURLButton");
		WebElement homepageUrl = driver.findElement(By.id("homepageURL"));
		homepageUrl.clear();
		homepageUrl.sendKeys(homepage);
		tick("toolbarShowHomeButton");
		untick("toolbarShowBookmarksBar");
		untick("instantEnabledCheckbox");

		// 2. Personal Stuff
		click("personalPageNav");

		if (Chromium.arguments.save_passwords)
			click("passwords-offersave");
		else
			click("passwords-neversave");
		untick("autofill-enabled");

		if (!"gnome-fallback".equals(System.getenv().get("DESKTOP_SESSION"))) {
			click("themes-reset");
		}

		// Closing nested subpages with the top-right X-button doesn't work
		// properly (the way I'm doing it with 'click'). Workaround: luckily
		// each such nested page has its own URL, so navigate to it.

		// 3. Under the Hood (chrome://settings/advanced)
		click("advancedPageNav");

		// 3.1 Content Settings (chrome://settings/content)
		click("privacyContentSettingsButton");

		clickX("//input[@name='cookies' and @value='session']");
		tickX("//input[@pref='profile.block_third_party_cookies']");
		tick("clear-cookies-on-exit");

		// 3.1.1 Cookies and Other Data (chrome://settings/cookies)
		click("show-cookies-button");

		click("remove-all-cookies-button");

		// Back to:
		// 3.1
		driver.get("chrome://settings/content");

		clickX("//input[@type='radio' and @name='handlers' and @value='block']");
		clickX("//input[@type='radio' and @name='location' and @value='block']");
		clickX("//input[@type='radio' and @name='notifications' and @value='block']");

		// Back to:
		// 3
		driver.get("chrome://settings/advanced");

		// 3.2 Clear Browsing Data
		click("privacyClearDataButton");

		clickX("//select[@id='clearBrowserDataTimePeriod']/option[@value='4']");
		tick("deleteBrowsingHistoryCheckbox");
		tick("deleteDownloadHistoryCheckbox");
		tick("deleteCacheCheckbox");
		tick("deleteCookiesCheckbox");
		tick("deleteFormDataCheckbox");

		click("clearBrowserDataCommit");
		waitForInvisibility("clearBrowserDataOverlay");

		// Back to:
		// 3

		untick("alternateErrorPagesEnabled");
		untick("searchSuggestEnabled");
		untick("dnsPrefetchingEnabled");
		tick("safeBrowsingEnabled");

		// 3.3 Fonts and Encoding (chrome://settings/fonts)
		driver.get("chrome://settings/fonts");

		clickXFirstFound(new String[] {
				"//select[@id='standard-font-family']/option[@value='Times New Roman']",
				"//select[@id='standard-font-family']/option[@value='DejaVu Serif']" });

		clickXFirstFound(new String[] {
				"//select[@id='serif-font-family']/option[@value='Times New Roman']",
				"//select[@id='serif-font-family']/option[@value='DejaVu Serif']" });

		clickXFirstFound(new String[] {
				"//select[@id='sans-serif-font-family']/option[@value='Arial']",
				"//select[@id='sans-serif-font-family']/option[@value='DejaVu Sans']" });

		clickXFirstFound(new String[] {
				"//select[@id='fixed-font-family']/option[@value='DejaVu Sans Mono']",
				"//select[@id='fixed-font-family']/option[@value='Monospace']" });

		clickX("//select[@id='font-encoding']/option[@value='UTF-8']");

		// Back to:
		// 3
		driver.get("chrome://settings/advanced");

		untick("enableTranslate");

		// Downloads
		// If we want to "Ask where to save each file before downloading"
		// tickX("//input[@type='checkbox' and @pref='download.prompt_for_download']");

		// Background Apps
		untick("backgroundModeCheckbox");

		// Close the browser
		driver.quit();
	}

}
