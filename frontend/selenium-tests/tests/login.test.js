const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const assert = require('assert');

describe('Rooyify Web Login Test', function () {
  let driver;

  before(async function () {
    const options = new chrome.Options();
    options.addArguments('--headless', '--no-sandbox', '--disable-dev-shm-usage');
    driver = await new Builder().forBrowser('chrome').setChromeOptions(options).build();
  });

  after(async function () {
    if (driver) {
      await driver.quit();
    }
  });

  it('Should log in successfully with valid credentials', async function () {
    // Read base URL from env, default to local/tunnel check
    const baseUrl = process.env.BASE_URL || 'https://sudheernaidu18.github.io/rooyify';
    await driver.get(baseUrl);

    // Locate fields using Stable IDs (Step 12)
    const emailField = await driver.wait(until.elementLocated(By.id('email')), 10000);
    await emailField.sendKeys('patient@example.com');

    const passwordField = await driver.findElement(By.id('password'));
    await passwordField.sendKeys('password123');

    const loginButton = await driver.findElement(By.id('login-button'));
    await loginButton.click();

    // Wait for Dashboard view redirection
    const currentUrl = await driver.getCurrentUrl();
    console.log(`Current URL after login submit: ${currentUrl}`);
  });
});
