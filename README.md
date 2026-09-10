# tilbakekreving-tvangsgrunnlag

API for å dele tvangsgrunnlag mellom NAV og Skatteetaten.

## Endepunkt

`POST /api/tilbakekreving/tvangsgrunnlag/v1`

Tar imot en `TvangsgrunnlagRequest` (skyldner, oppdragsgiversKravidentifikator,
skatteetatensKravidentifikator, valgfri fraOgMedDato) og returnerer en zip-fil med
tilbakekrevingsvedtak/endringsvedtak som PDF.

Krever headerne `Korrelasjonsid` og `Klientid`.

| Status | Betydning |
| --- | --- |
| 200 | Zip-fil med dokumenter |
| 400 | Ugyldig forespørsel |
| 404 | Tvangsgrunnlag ikke funnet |

Flyten i `TvangsgrunnlagService`:

1. Validerer request
2. Verifiserer krav i tilbakeløsningen (eller sokos-ske-krav for skatteetatensKravidentifikator)
3. Henter dokumenter, filtrert på `fraOgMedDato`
4. Zipper PDF-ene
5. Registrerer utlevering i Joark
6. Logger statistikk over utleverte vedtak

## Struktur

```
modell/    Request-, dokument- og feilklasser
klient/    Interfaces mot tilbakeløsningen, sokos-ske-krav, SAF og Joark
tjeneste/  TvangsgrunnlagService og statistikk
```

`klient`-implementasjonene (`Midlertidig*Client`) er placeholders uten ekte
integrasjon. Se `TODO`-kommentarer i hver fil for hva som gjenstår.

**Ikke implementert ennå:** Maskinporten/JWT-validering og audit-logging av
henvendelse/utlevert dokument. Merket med `TODO` i koden.

## Kjøre og teste

```bash
./gradlew run    # Starter appen på port 8080
./gradlew test   # Kjører tester
```

Swagger UI lokalt: http://localhost:8080/swagger

Swagger UI i preprod: https://tilbakekreving-tvangsgrunnlag.dev.intern.nav.no/swagger

## Teste med Postman

En ferdig Postman-request ligger i [`tvangsgrunnlag.postman_collection.json`](tvangsgrunnlag.postman_collection.json),
med korrekte headere og et kjent testkrav.

1. Start appen lokalt: `./gradlew run`
2. Åpne Postman, klikk **Import** øverst til venstre
3. Velg **File** og pek på `tvangsgrunnlag.postman_collection.json` i repoet
4. Klikk **Import** — collection'en `tilbakekreving-tvangsgrunnlag` dukker opp i sidepanelet
5. Åpne requesten "Hent tvangsgrunnlag (kjent krav)"
6. Trykk på pilen ved siden av **Send**-knappen og velg **Send and Download**
7. Velg hvor zip-fila skal lagres

Forventet resultat: status **200 OK** og en nedlastet zip med `dok-1.pdf` og `dok-2.pdf`.