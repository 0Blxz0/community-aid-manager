# Third-Party Code and Asset Citations

No third-party source code, images, icons, fonts, or datasets were copied into this project. The CSS, UML artwork, sample records, domain model, services, persistence layer, tests, and documentation were created specifically for this submission with declared AI assistance.

## Build dependencies

### ScalaFX 21.0.0-R32

- Purpose: Scala wrapper and domain-specific language for JavaFX user interfaces.
- Project: https://github.com/scalafx/scalafx
- Artifact: https://repo1.maven.org/maven2/org/scalafx/scalafx_3/21.0.0-R32/
- Licence: BSD 3-Clause License.
- Licence text: https://github.com/scalafx/scalafx/blob/master/LICENSE.txt

### OpenJFX / JavaFX 21

- Purpose: native desktop controls, graphics, layout, and event runtime used by ScalaFX.
- Project: https://openjfx.io/
- Source: https://github.com/openjdk/jfx
- Licence: GNU General Public License version 2 with the Classpath Exception.
- Licence text: https://github.com/openjdk/jfx/blob/master/LICENSE

### Scala 3.3.6

- Purpose: programming language and standard library.
- Project: https://www.scala-lang.org/
- Source: https://github.com/scala/scala3
- Licence: Apache License 2.0.
- Licence text: https://github.com/scala/scala3/blob/main/LICENSE

### MUnit 1.0.2

- Purpose: unit-test framework used only by `src/test/scala`.
- Project: https://scalameta.org/munit/
- Source: https://github.com/scalameta/munit
- Licence: Apache License 2.0.
- Licence text: https://github.com/scalameta/munit/blob/main/LICENSE

### SQLite JDBC 3.53.2.0

- Purpose: embedded SQLite database driver and JDBC interface used by the persistence layer.
- Project: https://github.com/xerial/sqlite-jdbc
- Artifact: https://central.sonatype.com/artifact/org.xerial/sqlite-jdbc/3.53.2.0
- Licence: Apache License 2.0; bundled SQLite-derived code also retains BSD 2-Clause notices.
- Licence texts: https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE and https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE.zentus

### SLF4J NOP 2.0.17

- Purpose: no-operation SLF4J provider used to keep the SQLite driver’s logging binding explicit.
- Project: https://www.slf4j.org/
- Artifact: https://central.sonatype.com/artifact/org.slf4j/slf4j-nop/2.0.17
- Licence: MIT License.
- Licence text: https://www.slf4j.org/license.html

## AI assistance

AI-assisted sections are documented in `ai/interaction_log.md` and tagged in source with `// ai-assisted: #N` plus an adjacent `// why:` explanation. AI is treated as a declared development tool, not as an external source whose output was accepted without review.
