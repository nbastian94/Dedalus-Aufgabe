import { expect, test } from '@playwright/test';

test('berechnet Stueckelung im Frontend-Modus', async ({ page }) => {
  await page.goto('/');
  await page.locator('#amount-input').fill('150.00');
  await page.getByTestId('calculate-button').click();

  await expect(page.getByTestId('breakdown-table')).toContainText('100.00');
  await expect(page.getByTestId('breakdown-table')).toContainText('50.00');
});

test('Slider passen Restbetrag und zusaetzliche Moeglichkeiten dynamisch an', async ({ page }) => {
  await page.goto('/');
  await page.locator('#amount-input').fill('150.00');
  await page.getByTestId('calculate-button').click();

  const slider100Handle = page.getByTestId('slider-100-00').locator('.p-slider-handle');
  await slider100Handle.focus();
  await page.keyboard.press('ArrowLeft');

  await expect(page.getByTestId('remaining-amount')).toContainText('100,00');
  await expect(page.getByTestId('additional-50-00')).toHaveText('2');
  await expect(page.getByTestId('additional-20-00')).toHaveText('5');
  await expect(page.getByTestId('apply-manual-button')).toBeDisabled();

  const slider50Handle = page.getByTestId('slider-50-00').locator('.p-slider-handle');
  await slider50Handle.focus();
  await page.keyboard.press('ArrowRight');
  await page.keyboard.press('ArrowRight');

  await expect(page.getByTestId('remaining-amount')).toContainText('0,00');
  await expect(page.getByTestId('apply-manual-button')).toBeEnabled();
});

test('Moduswechsel synchronisiert Frontend nach Backend via POST /calculations', async ({ page }) => {
  let postCalls = 0;
  let postedBody: unknown = null;

  await page.route('**/calculations', async (route) => {
    if (route.request().method() === 'POST') {
      postCalls += 1;
      postedBody = route.request().postDataJSON();
      await route.fulfill({ status: 204, body: '' });
      return;
    }
    await route.fulfill({ status: 204, body: '' });
  });

  await page.goto('/');
  await page.locator('#amount-input').fill('150.00');
  await page.getByTestId('calculate-button').click();
  await page.getByRole('button', { name: 'Backend' }).click();

  expect(postCalls).toBe(1);
  expect((postedBody as { amount: string }).amount).toBe('150.00');
});

test('Moduswechsel synchronisiert Backend nach Frontend via GET /calculations', async ({ page }) => {
  let getCalls = 0;

  await page.route('**/calculate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        amount: '150.00',
        breakdown: [
          { denomination: '100.00', count: 1 },
          { denomination: '50.00', count: 1 }
        ]
      })
    });
  });

  await page.route('**/calculations', async (route) => {
    if (route.request().method() === 'GET') {
      getCalls += 1;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          amount: '150.00',
          breakdown: [
            { denomination: '100.00', count: 1 },
            { denomination: '50.00', count: 1 }
          ]
        })
      });
      return;
    }
    await route.fulfill({ status: 204, body: '' });
  });

  await page.goto('/');
  await page.getByRole('button', { name: 'Backend' }).click();
  await page.locator('#amount-input').fill('150.00');
  await page.getByTestId('calculate-button').click();
  await page.getByRole('button', { name: 'Frontend' }).click();

  expect(getCalls).toBe(1);
  await expect(page.getByText('Vorherige Backend-Berechnung wurde in den Frontend-Modus uebernommen.')).toBeVisible();
});
