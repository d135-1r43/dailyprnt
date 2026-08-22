# dailyprnt — Requirements

## Concept

dailyprnt assembles a **daily strip of personal content**, in the spirit of Berg's
Little Printer and the [Nord Projects revival](https://nordprojects.co/projects/littleprinters/).

Each day has one **edition**: a single narrow, vertical strip built from independent
**modules**, each contributing one block of content. The long-term target is a thermal
receipt printer. For now the app delivers the edition as a web page shaped like the
printed strip, so the same markup can later be driven to real hardware.

## Glossary

| Term | Meaning |
| --- | --- |
| **Edition** | The complete strip for one calendar date. |
| **Module** | A self-contained content unit contributing one block to an edition (quote, weather, …). |
| **Strip** | The physical/visual form: fixed narrow width, unbounded length. |

## Functional requirements

**FR-1 — Daily edition.** The app serves the edition for a given date, defaulting to today.

**FR-2 — Composition from modules.** An edition is the ordered concatenation of its
enabled modules' rendered output, preceded by a masthead showing the title and date.

**FR-3 — Module contract.** A module renders itself to a fragment of strip markup and is
discovered automatically, so adding a module means adding a class and template — no edit
to the page assembling the edition.

**FR-4 — Configuration.** Which modules are enabled, their order, and their per-module
settings (quote topic, weather location) are configuration, not hardcoded values.

**FR-5 — Modules in scope.**
- **Quote** — an inspirational quote with author, on a configured topic. *(exists)*
- **Word of the Day** — word, pronunciation, part of speech, definition, example. *(exists, but see KI-1)*
- **Weather** — today's forecast for a configured location.

**FR-6 — Stable content per date.** An edition is generated once and stored. Re-requesting
the same date returns identical content rather than regenerating it. This keeps a printed
strip and the screen in agreement, makes history browsable, and avoids paying for the same
AI call twice.

**FR-6a — Concurrent requests.** Two requests for the same date arriving together must
both succeed and print the same edition. Exactly one is stored.

**FR-7 — Module failure isolation.** A module that fails — API error, timeout, bad
credentials — must degrade to a placeholder block within the edition. It must never
prevent the rest of the edition from rendering. *(Currently violated: see KI-2.)*

## Non-functional requirements

**NFR-1 — Strip geometry.** Content is laid out for a **58 mm thermal printer: 384 dots
wide at 203 dpi**, i.e. a 384 px content column. Length is unbounded.

**NFR-2 — Monochrome.** Thermal output is 1-bit. Modules must stay legible with no colour
and no greyscale: no information may be carried by colour alone, and no background fills,
photographs, or hairlines thinner than 1 px may be required to read a module.

**NFR-3 — Cost control.** Rendering an edition must not re-invoke paid AI or weather APIs
for content already generated for that date (follows from FR-6).

**NFR-4 — Print fidelity.** The web rendering is a preview of the printed artefact, so its
column width and typography must match what the printer will produce, rather than being an
independently designed web page.

## Out of scope for now

- Driving physical hardware (ESC/POS, USB/serial/network transport).
- Multiple users, accounts, authentication, per-user subscriptions.
- Push delivery or scheduled pre-generation — editions are generated lazily on first request.
- Any module beyond the three named in FR-5.

## Assumptions in force

These were decided by default and are implemented as described. They are still open to
revision, but changing them now means changing code:

- **A-1** — Target width is 58 mm / 384 px (NFR-1) rather than 80 mm / 576 px.
- **A-2** — Editions are generated lazily on first request and persisted (FR-6), rather
  than by a scheduled job that pre-generates ahead of time.
- **A-3** — Single user; configuration lives in `application.properties` (FR-4) rather than
  a per-user subscription model.

## Adding a module

1. Implement `com.dailyprnt.modules.Module`, returning a stable `id()`.
2. Add a Qute template under `templates/com/dailyprnt/modules/<id>/module.html`.
3. Style it in `templates/daily.html` under a `.<id>` selector, keeping to NFR-2.
4. Add the id to `dailyprnt.modules`, in the position it should print.

Nothing else needs editing: the edition assembles whatever the configuration names.
