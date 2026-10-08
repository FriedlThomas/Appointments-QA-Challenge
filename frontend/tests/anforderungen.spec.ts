import { expect, test, type APIRequestContext } from '@playwright/test'

type Slot = {
  id: string
  arzt: string
  beginn: string
}

type Termin = {
  id: string
  slotId: string
}

/**
 * Erstellt einen Slot mit relativem Beginn, damit die Tests unabhaengig von
 * Seed-Daten und Tagesdatum laufen.
 */
async function neuerSlot(request: APIRequestContext, minutenAbJetzt: number): Promise<Slot> {
  const beginn = new Date(Date.now() + minutenAbJetzt * 60 * 1000)
  const pad = (wert: number) => String(wert).padStart(2, '0')
  const lokal =
    `${beginn.getFullYear()}-${pad(beginn.getMonth() + 1)}-${pad(beginn.getDate())}` +
    `T${pad(beginn.getHours())}:${pad(beginn.getMinutes())}:${pad(beginn.getSeconds())}`

  const antwort = await request.post('/api/slots', {
    data: { arzt: 'Dr. Testlauf', beginn: lokal },
  })
  expect(antwort.status()).toBe(201)
  return await antwort.json() as Slot
}

async function buchen(
  request: APIRequestContext,
  slotId: string,
  patientenname = 'Testpatient',
  grund = 'Kontrolle',
) {
  return request.post('/api/appointments', {
    data: { slotId, patientenname, grund },
  })
}

async function terminBuchen(
  request: APIRequestContext,
  slotId: string,
  patientenname = 'Testpatient',
  grund = 'Kontrolle',
): Promise<Termin> {
  const antwort = await buchen(request, slotId, patientenname, grund)
  expect(antwort.status()).toBe(201)
  return await antwort.json() as Termin
}

async function expectFehler(antwort: Awaited<ReturnType<typeof buchen>>, status: number) {
  expect(antwort.status()).toBe(status)
  expect(await antwort.json()).toEqual({ fehler: expect.any(String) })
}

test('bucht einen freien Termin ueber die Oberflaeche', async ({ page, request }) => {
  const slot = await neuerSlot(request, 72 * 60)
  const patient = `Testpatient ${Date.now()}`

  await page.goto('/')
  await page.getByLabel('Termin-ID').fill(slot.id)
  await page.getByLabel('Patientenname').fill(patient)
  await page.getByLabel('Grund des Besuchs').fill('Jahreskontrolle')
  await page.getByRole('button', { name: 'Termin buchen' }).click()

  await expect(page.getByRole('status')).toHaveText('Termin gebucht.')
  await expect(page.getByRole('cell', { name: patient })).toBeVisible()
})

test('lehnt eine zweite Buchung desselben Slots mit 409 ab', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)

  expect((await buchen(request, slot.id, 'Erste Buchung')).status()).toBe(201)
  await expectFehler(await buchen(request, slot.id, 'Zweite Buchung'), 409)
})

test('laesst auch bei gleichzeitigen Buchungen nur eine Buchung zu', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)

  const antworten = await Promise.all([
    buchen(request, slot.id, 'Erste gleichzeitige Buchung'),
    buchen(request, slot.id, 'Zweite gleichzeitige Buchung'),
  ])

  expect(antworten.map((antwort) => antwort.status()).sort()).toEqual([201, 409])
})

test('lehnt einen Slot aus der Vergangenheit mit 400 ab', async ({ request }) => {
  const slot = await neuerSlot(request, -10)

  await expectFehler(await buchen(request, slot.id), 400)
})

test('liefert fuer eine unbekannte Slot-ID 404', async ({ request }) => {
  await expectFehler(await buchen(request, 'unbekannter-slot'), 404)
})

test('liefert fuer eine unbekannte Termin-ID beim Stornieren 404', async ({ request }) => {
  const antwort = await request.delete('/api/appointments/unbekannter-termin')

  expect(antwort.status()).toBe(404)
  expect(await antwort.json()).toEqual({ fehler: expect.any(String) })
})

test('gibt den Slot nach einer Stornierung zur erneuten Buchung frei', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)
  const termin = await terminBuchen(request, slot.id)

  expect((await request.delete(`/api/appointments/${termin.id}`)).status()).toBe(200)
  expect((await buchen(request, slot.id, 'Nach Stornierung')).status()).toBe(201)
})

test('lehnt eine zweite Stornierung desselben Termins mit 409 ab', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)
  const termin = await terminBuchen(request, slot.id)

  expect((await request.delete(`/api/appointments/${termin.id}`)).status()).toBe(200)
  await expectFehler(await request.delete(`/api/appointments/${termin.id}`), 409)
})

test('lehnt die Stornierung weniger als 24 Stunden vor Beginn mit 409 ab', async ({ request }) => {
  const slot = await neuerSlot(request, 23 * 60 + 59)
  const termin = await terminBuchen(request, slot.id)

  await expectFehler(await request.delete(`/api/appointments/${termin.id}`), 409)
})

test('erlaubt die Stornierung knapp mehr als 24 Stunden vor Beginn', async ({ request }) => {
  const slot = await neuerSlot(request, 24 * 60 + 5)
  const termin = await terminBuchen(request, slot.id)

  expect((await request.delete(`/api/appointments/${termin.id}`)).status()).toBe(200)
})

test('akzeptiert Patientenname mit 1 oder 100 Zeichen', async ({ request }) => {
  for (const laenge of [1, 100]) {
    const slot = await neuerSlot(request, 4 * 24 * 60)
    expect((await buchen(request, slot.id, 'P'.repeat(laenge))).status()).toBe(201)
  }
})

test('lehnt leeren oder laengeren Patientenname als 100 Zeichen mit 400 ab', async ({ request }) => {
  for (const patientenname of ['', 'P'.repeat(101)]) {
    const slot = await neuerSlot(request, 4 * 24 * 60)
    await expectFehler(await buchen(request, slot.id, patientenname), 400)
  }
})

test('akzeptiert einen Besuchsgrund mit 255 Zeichen', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)

  expect((await buchen(request, slot.id, 'Testpatient', 'G'.repeat(255))).status()).toBe(201)
})

test('lehnt einen leeren Besuchsgrund mit 400 ab', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)

  await expectFehler(await buchen(request, slot.id, 'Testpatient', ''), 400)
})

test('lehnt einen Besuchsgrund mit mehr als 255 Zeichen mit 400 ab', async ({ request }) => {
  const slot = await neuerSlot(request, 4 * 24 * 60)

  await expectFehler(await buchen(request, slot.id, 'Testpatient', 'G'.repeat(256)), 400)
})
