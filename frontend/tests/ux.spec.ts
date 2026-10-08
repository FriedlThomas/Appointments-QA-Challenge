import { expect, test, type APIRequestContext } from '@playwright/test'

async function neuerSlot(request: APIRequestContext, stundenInDerZukunft = 96) {
  const beginn = new Date(Date.now() + stundenInDerZukunft * 60 * 60 * 1000)
  const pad = (wert: number) => String(wert).padStart(2, '0')
  const lokal =
    `${beginn.getFullYear()}-${pad(beginn.getMonth() + 1)}-${pad(beginn.getDate())}` +
    `T${pad(beginn.getHours())}:${pad(beginn.getMinutes())}:${pad(beginn.getSeconds())}`

  const antwort = await request.post('/api/slots', {
    data: { arzt: `UX-Test ${Date.now()}`, beginn: lokal },
  })
  expect(antwort.status()).toBe(201)
  return await antwort.json() as { id: string; arzt: string; beginn: string }
}

test('zeigt dieselbe Anzahl freier Termine wie die API', async ({ page, request }) => {
  const antwort = await request.get('/api/slots')
  expect(antwort.ok()).toBeTruthy()
  const slots = await antwort.json() as Array<{ gebucht: boolean }>
  const erwarteteAnzahl = slots.filter((slot) => !slot.gebucht).length

  await page.goto('/')
  const freieTermineBereich = page.locator('section').filter({
    has: page.getByRole('heading', { name: 'Freie Termine' }),
  })

  if (erwarteteAnzahl === 0) {
    await expect(freieTermineBereich.getByText('Derzeit sind keine freien Termine verfuegbar.'))
      .toBeVisible()
    await expect(freieTermineBereich.getByRole('table')).toHaveCount(0)
  } else {
    await expect(freieTermineBereich.getByRole('row')).toHaveCount(erwarteteAnzahl + 1)
  }
})

test('zeigt dieselbe Anzahl Meine-Termine-Eintraege wie die API', async ({ page, request }) => {
  const antwort = await request.get('/api/appointments')
  expect(antwort.ok()).toBeTruthy()
  const termine = await antwort.json() as Array<{ id: string }>

  await page.goto('/')
  const meineTermineBereich = page.locator('section').filter({
    has: page.getByRole('heading', { name: 'Meine Termine' }),
  })

  if (termine.length === 0) {
    await expect(meineTermineBereich.getByText('Es sind keine Termine gebucht.'))
      .toBeVisible()
    await expect(meineTermineBereich.getByRole('table')).toHaveCount(0)
  } else {
    await expect(meineTermineBereich.getByRole('row')).toHaveCount(termine.length + 1)
  }
})

test('zeigt Arzt und Beginn eines gebuchten Termins unter Meine Termine an', async ({ page, request }) => {
  const slot = await neuerSlot(request)
  const patient = `Terminanzeige ${Date.now()}`
  const buchung = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: patient, grund: 'UX-Test' },
  })
  expect(buchung.status()).toBe(201)

  await page.goto('/')
  const terminZeile = page.getByRole('row').filter({ hasText: patient })
  await expect(terminZeile).toBeVisible()

  const erwarteterBeginn = await page.evaluate(
    (beginn) => new Date(beginn).toLocaleString('de-AT'),
    slot.beginn,
  )
  await expect.soft(terminZeile.getByRole('cell').nth(0)).toHaveText(slot.arzt)
  await expect.soft(terminZeile.getByRole('cell').nth(1)).toHaveText(erwarteterBeginn)
})

test('begrenzt die Tabellenspalten bei langen Patienten- und Besuchsgrundtexten', async ({ page, request }) => {
  const slot = await neuerSlot(request)
  const patient = `P${'a'.repeat(99)}`
  const grund = `G${'r'.repeat(254)}`
  const buchung = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: patient, grund },
  })
  expect(buchung.status()).toBe(201)

  await page.setViewportSize({ width: 1024, height: 768 })
  await page.goto('/')
  const termineSection = page.locator('section').filter({
    has: page.getByRole('heading', { name: 'Meine Termine' }),
  })
  const tabelle = termineSection.getByRole('table')
  const groessen = await termineSection.evaluate((section) => {
    const table = section.querySelector('table')
    if (!table) throw new Error('Tabelle "Meine Termine" wurde nicht gefunden.')
    return {
      sectionBreite: section.clientWidth,
      sectionScrollBreite: section.scrollWidth,
      tabellenBreite: table.getBoundingClientRect().width,
    }
  })

  expect.soft(groessen.tabellenBreite).toBeLessThanOrEqual(groessen.sectionBreite)
  expect.soft(groessen.sectionScrollBreite).toBeLessThanOrEqual(groessen.sectionBreite)
  await expect(tabelle.getByRole('cell', { name: patient })).toBeVisible()
  await expect(tabelle.getByRole('cell', { name: grund })).toBeVisible()
})

test('stellt die Buchungsoberflaeche mit beschrifteten Pflichtfeldern bereit', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Terminbuchung', level: 1 })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Freie Termine' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Termin buchen' })).toBeVisible()
  await expect(page.getByLabel('Termin-ID')).toHaveAttribute('required', '')
  await expect(page.getByLabel('Patientenname')).toHaveAttribute('required', '')
  await expect(page.getByLabel('Grund des Besuchs')).toHaveAttribute('required', '')
  await expect(page.getByRole('button', { name: 'Termin buchen' })).toBeVisible()
})

test('waehlt einen freien Slot aus und bestaetigt die Buchung sichtbar', async ({ page, request }) => {
  const slot = await neuerSlot(request)
  const patient = `UX Patient ${Date.now()}`
  const freieSlotZeile = page.getByRole('row').filter({ hasText: slot.arzt })

  await page.goto('/')
  await freieSlotZeile.getByRole('button', { name: 'Auswaehlen' }).click()
  await expect(page.getByLabel('Termin-ID')).toHaveValue(slot.id)
  await page.getByLabel('Patientenname').fill(patient)
  await page.getByLabel('Grund des Besuchs').fill('UX-Test')
  await page.getByRole('button', { name: 'Termin buchen' }).click()

  await expect(page.getByRole('status')).toHaveText('Termin gebucht.')
  await expect(page.getByRole('cell', { name: patient })).toBeVisible()
  await expect(page.getByLabel('Termin-ID')).toHaveValue('')
  await expect(freieSlotZeile).toHaveCount(0)
})

test('zeigt bei fehlgeschlagener Buchung eine zugaengliche Fehlermeldung', async ({ page, request }) => {
  const slot = await neuerSlot(request)
  const patient = `Doppelt ${Date.now()}`
  const ersteBuchung = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: patient, grund: 'Kontrolle' },
  })
  expect(ersteBuchung.status()).toBe(201)

  await page.goto('/')
  await page.getByLabel('Termin-ID').fill(slot.id)
  await page.getByLabel('Patientenname').fill(`Zweite ${patient}`)
  await page.getByLabel('Grund des Besuchs').fill('Kontrolle')
  await page.getByRole('button', { name: 'Termin buchen' }).click()

  await expect(page.getByRole('alert')).toBeVisible()
  await expect(page.getByRole('alert')).not.toBeEmpty()
  await expect(page.getByLabel('Termin-ID')).toHaveValue(slot.id)
  await expect(page.getByLabel('Patientenname')).toHaveValue(`Zweite ${patient}`)
})

test('zeigt nach dem Stornieren die Erfolgsmeldung und den Slot wieder als frei', async ({ page, request }) => {
  const slot = await neuerSlot(request)
  const patient = `Storno UX ${Date.now()}`
  const buchung = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: patient, grund: 'Kontrolle' },
  })
  expect(buchung.status()).toBe(201)

  await page.goto('/')
  const terminZeile = page.getByRole('row').filter({ hasText: patient })
  await terminZeile.getByRole('button', { name: 'Stornieren' }).click()

  await expect(page.getByRole('status')).toHaveText('Termin storniert.')
  await expect(terminZeile).toBeVisible()
  await expect(terminZeile.getByRole('button', { name: 'Stornieren' })).toHaveCount(0)
  await expect(
    page.getByRole('row').filter({ hasText: slot.arzt })
      .getByRole('button', { name: 'Auswaehlen' }),
  ).toBeVisible()
})
