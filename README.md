# IFT3913 – Tâche 2 : augmentation d'une suite de tests avec ChatUniTest et PIT

**Binôme :** Messaad Abdelmouhcine (`abdel2154`) · Cherir Roustom Abdeldjalel (`R0us24`)
**Cas d'étude :** Apache Tika 4.0.0-SNAPSHOT (fork de [umontreal-diro/tika](https://github.com/umontreal-diro/tika)), module `tika-core`
**Classes testées :** `org.apache.tika.io.EndianUtils` et `org.apache.tika.io.FilenameUtils`

> Le README original d'Apache Tika a été déplacé dans [README-TIKA.md](README-TIKA.md).

## Résumé des résultats

| | EndianUtils | FilenameUtils |
|---|---|---|
| Couverture de lignes (JaCoCo) avant → après | 32/126 (25 %) → **125/126 (99 %)** | 161/184 (87 %) → **171/184 (93 %)** |
| Méthodes couvertes avant → après | 4/32 → **32/32** | 13/14 → 13/14 |
| Score de mutation PIT, tests originaux | 38/207 (**18 %**) | 73/115 (**63 %**) |
| Score de mutation PIT, + tests ChatUniTest | 138/207 (**67 %**) | 73/115 (**63 %**) |
| Score de mutation PIT, + tests écrits à la main | 205/207 (**99 %**) | 97/115 (**84 %**) |

Tous les tests du module `tika-core` (814 tests, dont 52 générés et 13 écrits à la main) passent localement et dans la GitHub Action [`tache2.yml`](.github/workflows/tache2.yml).

---

## 1. Choix des classes

Nous avons choisi le module `tika-core` parmi les 5 modules autorisés. Il est léger, compile vite et ne demande pas de fichiers de test lourds (PDF, Office, OCR). Dans ce module, nous avons cherché des classes **déjà testées**, mais **mal couvertes**, et avec des **mutants vivants**.

### `EndianUtils`
Classe utilitaire qui lit des entiers (16, 32 et 64 bits, signés ou non, little/big/middle-endian) dans un flux ou un tableau d'octets. Elle sert aux parseurs de formats binaires.

- La classe de test `EndianUtilsTest` existe, mais ne contient que **4 tests**, sur `readUE7`, `readUIntLE`, `readUIntBE` et `readIntME`.
- JaCoCo (avant) : **32 lignes sur 126 couvertes (25 %)**, **4 méthodes sur 32**, 10 branches sur 28.
- PIT (avant) : **207 mutants, 38 tués (18 %)**, 14 survivants et **155 non couverts**.
- Ce sont des calculs purs sur des bits (`<<`, `&`, `|`, `+`), donc beaucoup de mutants arithmétiques. C'est idéal pour évaluer la qualité des oracles.

### `FilenameUtils`
Classe qui nettoie des noms et chemins de fichiers (caractères réservés, chemins Windows, attaques « zip slip ») et calcule des extensions.

- La classe de test `FilenameUtilsTest` contient **10 tests**.
- JaCoCo (avant) : **161 lignes sur 184 (87 %)**, 82 branches sur 110 (75 %). La méthode publique `calculateExtension` n'est jamais appelée directement par un test.
- PIT (avant) : **115 mutants, 73 tués (63 %)**, **20 survivants** et 22 non couverts. Les survivants se trouvent notamment dans les conditions limites (`>` / `>=`) et dans l'ordre de repli des métadonnées (`getEmbeddedName`, `getEmbeddedPath`).

Rapports complets « avant » : [`tache2-rapports/jacoco-avant/`](tache2-rapports/jacoco-avant/) et [`tache2-rapports/pit-avant/`](tache2-rapports/pit-avant/). Ce sont des fichiers HTML, à télécharger et ouvrir dans un navigateur ; `mutations.csv` est lisible directement.

---

## 2. Installation de ChatUniTest dans le pipeline Maven

### Modèle de langage local
- **Ollama** installé sous Windows. Modèle **`qwen2.5-coder:7b`** (7,6 milliards de paramètres, quantification Q4_K_M, environ 4,7 Go), exécuté localement sur CPU.
- Ollama expose une API compatible OpenAI sur `http://localhost:11434/v1/chat/completions`.

### Plugin Maven
Nous avons ajouté un **profil Maven `tache2`** dans [`tika-core/pom.xml`](tika-core/pom.xml), activé avec `-Ptache2`, pour ne pas perturber le build normal de Tika. Il contient :

```xml
<plugin>
  <groupId>io.github.zju-aces-ise</groupId>
  <artifactId>chatunitest-maven-plugin</artifactId>
  <version>2.1.1</version>
  <configuration>
    <url>http://localhost:11434/v1/chat/completions</url>
    <model>codeqwen:v1.5-chat</model>
    <apiKeys>ollama</apiKeys>
    <testNumber>1</testNumber>
    <maxRounds>2</maxRounds>
    <temperature>0.2</temperature>
    <thread>false</thread>
    <tmpOutput>${project.build.directory}/chatunitest-info</tmpOutput>
  </configuration>
</plugin>
```

ainsi que la dépendance `io.github.ZJU-ACES-ISE:chatunitest-starter:1.5.0`, exigée par la documentation de ChatUniTest.

**Problèmes rencontrés et solutions :**
1. **Nom de modèle refusé.** ChatUniTest 2.1.1 n'accepte qu'une liste fixe de noms (`gpt-*`, `code-llama`, `codeqwen:v1.5-chat`). Il a refusé `qwen2.5-coder:7b` (*« No Model with name qwen2.5-coder:7b »*). Nous avons créé un **alias Ollama** : `ollama cp qwen2.5-coder:7b codeqwen:v1.5-chat`. Le modèle réellement utilisé reste donc **qwen2.5-coder:7b**.
2. **Fichiers temporaires hors du projet.** Par défaut, le plugin écrit dans `/tmp/chatunitest-info` (soit `C:\tmp` sous Windows). Nous l'avons redirigé vers `target/` avec `tmpOutput`.
3. **Temps de génération.** Sur notre machine (CPU seulement), une génération complète de `EndianUtils` (32 méthodes × 2 tests, 3 rondes de réparation) était estimée à plus de 2 heures. Nous avons réduit à **1 test par méthode, 2 rondes de réparation**, sur **14 méthodes ciblées** (10 de `EndianUtils` et 4 de `FilenameUtils`) choisies parmi les méthodes non couvertes ou faiblement couvertes. La génération a été lancée méthode par méthode avec le script [`tache2-rapports/generer.ps1`](tache2-rapports/generer.ps1) :

```bash
mvn -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method -DselectMethod=EndianUtils#readIntLE
```

La génération des 14 méthodes a pris environ 1 heure (de 19 h 34 à 20 h 31).

---

## 3. Résultat de la génération

### Où sont les tests générés ?
- **Sortie brute de ChatUniTest :** `tika-core/chatunitest-tests/org/apache/tika/io/`. ChatUniTest n'y écrit que les tests qu'il considère réussis. Une copie non modifiée se trouve dans [`tache2-rapports/chatunitest-bruts/`](tache2-rapports/chatunitest-bruts/).
- **Tentatives qui n'ont pas compilé** (code et message d'erreur du compilateur) : [`tache2-rapports/chatunitest-erreurs/`](tache2-rapports/chatunitest-erreurs/), et [`chatunitest-essai1-erreurs/`](tache2-rapports/chatunitest-essai1-erreurs/) pour le premier essai, en mode classe complète, interrompu.
- **Version intégrée au projet (après corrections) :** `tika-core/src/test/java/org/apache/tika/io/*_Test.java` (14 fichiers, 52 tests). Chaque correction y est signalée par un commentaire `// CORRECTION n`.
- Journaux de génération : [`tache2-rapports/logs/`](tache2-rapports/logs/).

### Compilent-ils et s'exécutent-ils sans intervention ? **Non.**

| Méthode ciblée | Selon ChatUniTest | Réalité dans le projet | Corrections manuelles |
|---|---|---|---|
| `EndianUtils.readUShortBE` | échec de compilation (2 rondes) | réparé | 4 |
| `EndianUtils.readIntLE` | échec de compilation | réparé | 4 |
| `EndianUtils.readLongLE` | échec de compilation | réparé | 2 |
| `EndianUtils.readLongBE` | échec de compilation | réparé | 4 |
| `EndianUtils.getShortBE` (2 surcharges, 2 fichiers) | « compile and execute successfully » | **4 assertions fausses** | 4 |
| `EndianUtils.getIntBE` | succès | passe | 0 |
| `EndianUtils.getUIntLE` | succès (après 2 rondes) | passe | 0 |
| `EndianUtils.getLongLE` | « generated successfully » | passe | 0 |
| `EndianUtils.ubyteToInt` | succès | passe | 0 |
| `EndianUtils.getUByte` | succès | passe | 0 |
| `FilenameUtils.getName` | succès | **4 assertions fausses** | 4 |
| `FilenameUtils.getSuffixFromPath` | succès | **2 assertions fausses** | 2 |
| `FilenameUtils.normalize` | succès | **24 assertions fausses** | 24 |
| `FilenameUtils.calculateExtension` | échec de compilation | **abandonné** (à réécrire entièrement) | — |
| *Tous les fichiers* | | aucun ne compile sans **Mockito** | 1 (dépendance ajoutée au `pom.xml`) |

**Bilan :**
- **Aucun** test généré ne compile tel quel dans le projet. Tous importent `org.mockito.*`, alors que `tika-core` ne dépend pas de Mockito. Nous avons ajouté `mockito-core` et `mockito-junit-jupiter` (scope `test`) au `pom.xml` (1 correction).
- Sur les 14 méthodes, **5 n'ont produit aucun test compilable** malgré les rondes de réparation automatiques, et **5 autres avaient des assertions fausses**. Seuls **5 fichiers** passaient sans autre retouche que Mockito.
- Au total, **48 corrections manuelles** dans les tests, plus la dépendance Mockito. Un test (`calculateExtension`) a été abandonné.
- **Observation importante :** pour `getShortBE`, `getName`, `getSuffixFromPath` et `normalize`, le journal de ChatUniTest affiche *« compile and execute successfully »*, mais **13 tests échouent** dès qu'on les exécute avec Maven Surefire. On ne peut donc pas se fier au verdict de l'outil : il faut réexécuter les tests générés.

### Types d'erreurs de compilation (code généré par le LLM)
1. **Classes imaginaires ou mal localisées :** `org.apache.tika.io.InputStream`, `org.apache.tika.io.InputStreams`, `org.apache.tika.io.BufferUnderrunException`, `org.apache.tika.exception.BufferUnderrunException`. La vraie classe est la classe imbriquée `EndianUtils.BufferUnderrunException`. Le modèle n'a jamais réussi à corriger cette erreur, même quand le message du compilateur la lui indiquait.
2. **Mauvaise exception :** `java.nio.BufferUnderflowException` (nom proche, mais autre classe).
3. **Exception vérifiée non déclarée :** `BufferUnderrunException` hérite de `TikaException`. Les méthodes de test ne déclaraient que `throws IOException`.
4. **Variable non effectivement finale** utilisée dans une lambda `assertThrows`.
5. **Appel ambigu :** `metadata.set(Metadata.CONTENT_TYPE, null)` est ambigu entre `set(Property, Date)` et `set(Property, Calendar)`.

---

## 4. Critique des oracles : IA contre tests écrits à la main

### Ce qui est bien dans les tests générés
- **Oracles spécifiques** dans la majorité des cas : valeurs exactes avec `assertEquals`, par exemple `assertEquals(0x01020304, EndianUtils.getIntBE(data, 0))`. Les octets distincts (`01 02 03 04`) permettent de détecter une inversion d'ordre ou une erreur de décalage. C'est ce qui a tué 73 mutants arithmétiques.
- **Tests de robustesse** que les tests originaux n'avaient pas : décalage négatif, décalage hors du tableau, tableau vide, tableau `null` (`assertThrows(IndexOutOfBoundsException.class, ...)`).

### Ce qui est mauvais
1. **Oracles faux par incompréhension de la spécification.** Le modèle « devine » le résultat au lieu de le déduire :
   - **endianness :** pour `getShortBE({0x00, 0x01})`, il attend `0x0100` ; le bon résultat big-endian est `0x0001`. Pour `readLongLE(1..8)`, il attend `0x0102030405060708L` (ordre big-endian) au lieu de `0x0807060504030201L` ;
   - **`normalize` :** il suppose que l'espace et `/` sont des caractères réservés (`"example%20txt"`), alors que la liste `RESERVED_FILENAME_CHARACTERS` ne les contient pas. Résultat : 24 assertions fausses ;
   - **`getName(".file.txt")` :** il attend `""` (il confond avec le cas `"."` ou `".."`) ;
   - **`getSuffixFromPath("example.docx")` :** il attend `""`, alors que la Javadoc dit qu'une extension alphanumérique de 5 caractères ou moins (point inclus) est acceptée ;
   - **octet `0xFF` :** il croit qu'un octet `-1` signifie « fin de flux » et attend une exception, alors que `read()` renvoie `255` pour cet octet. Seule la valeur `-1` retournée par `read()` signale la fin.
2. **Oracles triviaux ou tautologiques :**
   - `ubyteToInt` : `int result = (int) b & 0xFF; assertEquals(result, EndianUtils.ubyteToInt(b));`. L'oracle **recopie l'implémentation**, donc le test vérifie le code avec lui-même ;
   - `getUByte` (décalage invalide) : un `try { ... } catch (ArrayIndexOutOfBoundsException e) {}` **sans `fail()`**. Le test passe que l'exception soit levée ou non ;
   - `getUIntLE` : les tests 2 et 3 sont **identiques**, et aucun ne teste un octet de poids fort à 1. Or c'est justement ce qui distingue un entier *non signé*.
3. **Bruit :** `@ExtendWith(MockitoExtension.class)` et `@Mock InputStream` jamais utilisés ; tests en double dans `getName` (`testGetNameWithDotPath` et `testGetNameWithSingleDotPath` sont identiques) ; méthode publique appelée par réflexion (`invokePrivateMethod`) dans `getSuffixFromPath` ; instanciation inutile de la classe utilitaire (`new EndianUtils()`).
4. **Mocks inutiles :** le test de `calculateExtension` « mocke » un `MimeTypes` qui n'est jamais injecté dans la méthode (celle-ci utilise un registre statique). Ce test ne pouvait pas fonctionner, même s'il avait compilé.

### Comparaison avec les tests écrits à la main (Tika)
- Les tests originaux utilisent des **données issues du domaine** : par exemple `0xF0 0xFF 0xFF 0xFF` → `4294967280L`, qui vérifie explicitement le caractère *non signé* de `readUIntLE`. `FilenameUtilsTest` vérifie de vrais chemins malveillants (`../../../`, chemins Windows, protocoles). Le modèle n'a jamais produit ce type de cas.
- Les tests originaux sont **peu nombreux mais ciblés**. Les tests générés sont **nombreux mais redondants**.
- Les tests manuels ne sont pas parfaits non plus : `EndianUtilsTest.testReadUIntBE` contient un **copier-coller erroné**. Sa vérification de l'exception appelle `readUIntLE` au lieu de `readUIntBE`, donc la détection de fin de flux de `readUIntBE` n'était pas testée. Nous ne l'avons pas modifié, pour garder les tests originaux intacts.

---

## 5. Ajout de PIT et analyse de mutation

PIT (`pitest-maven` 1.30.0 avec `pitest-junit5-plugin` 1.2.3) est configuré dans le même profil `tache2`, avec `targetClasses = EndianUtils*, FilenameUtils` et `targetTests = org.apache.tika.io.*`. Mutateurs par défaut.

```bash
mvn -pl tika-core -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage
```

| Exécution | Tests | EndianUtils | FilenameUtils | Total |
|---|---|---|---|---|
| 1. Tests originaux | `EndianUtilsTest`, `FilenameUtilsTest`, autres tests de `io` | 38/207 = **18 %** | 73/115 = **63 %** | 111/322 = 34 % |
| 2. + tests générés | + 14 fichiers `*_Test` | 138/207 = **67 %** | 73/115 = **63 %** | 211/322 = 66 % |
| 3. + tests manuels | + `EndianUtilsManualTest`, `FilenameUtilsManualTest` | 205/207 = **99 %** | 97/115 = **84 %** | 302/322 = 94 % |

Rapports : [`pit-avant/`](tache2-rapports/pit-avant/), [`pit-apres-generation/`](tache2-rapports/pit-apres-generation/), [`pit-final/`](tache2-rapports/pit-final/). Chaque dossier contient `index.html` et `mutations.csv`.

**Les tests générés ne détectent pas tous les mutants :** après l'exécution 2, il reste 34 mutants vivants et 35 non couverts dans `EndianUtils`, et 20 vivants et 22 non couverts dans `FilenameUtils`.

---

## 6. Mutants détectés par les tests générés, et pourquoi

Les tests générés tuent **100 nouveaux mutants**, tous dans `EndianUtils` :

| Méthode | Mutants tués | Test générateur |
|---|---|---|
| `readLongBE` | 17 | `EndianUtils_readLongBE_10_0_Test` |
| `readLongLE` | 17 | `EndianUtils_readLongLE_9_0_Test` |
| `getIntBE` | 14 | `EndianUtils_getIntBE_23_0_Test` |
| `getIntLE` | 14 | `EndianUtils_getUIntLE_24_0_Test` (via `getUIntLE`) |
| `getLongLE` | 9 | `EndianUtils_getLongLE_28_0_Test` |
| `readIntLE` | 9 | `EndianUtils_readIntLE_6_0_Test` |
| `getUShortBE` | 6 | `EndianUtils_getShortBE_16/17_0_Test` (via `getShortBE`) |
| `readUShortBE` | 5 | `EndianUtils_readUShortBE_3_0_Test` |
| `getUIntLE` | 3 | `EndianUtils_getUIntLE_24_0_Test` |
| `ubyteToInt`, `getShortBE`, `getUByte` | 2 chacun | tests correspondants |

Par type de mutateur :
- **MathMutator (73 mutants)** : PIT remplace `<<` par `>>`, `+` par `-`, `&` par `|`, etc., dans les expressions comme `(ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4`. Ils sont **tués parce que les tests utilisent des octets tous différents** (`01 02 03 04 ...`) et comparent la valeur exacte avec `assertEquals`. Toute modification d'un décalage ou d'un opérateur déplace ou altère un octet, et l'assertion échoue.
- **PrimitiveReturnsMutator (14)** : `return x` devient `return 0`. Tués parce que la valeur attendue n'est jamais 0.
- **IncrementsMutator (7)** : `data[i++]` devient `data[i--]`. Tués parce que l'octet lu ensuite n'est plus le bon, et la valeur calculée change.
- **NegateConditionalsMutator (5)** : `if ((ch1 | ... ) < 0) throw` devient `>= 0`. Une entrée **valide** lève alors une exception, et le test, qui attend une valeur, échoue.
- **ConditionalsBoundaryMutator (1)** : tué par un test de fin de flux.

**Aucun nouveau mutant n'est tué dans `FilenameUtils`.** Les tests générés qui ont fonctionné visent `getName`, `normalize` et `getSuffixFromPath`, que `FilenameUtilsTest` couvrait déjà très bien. Le seul test qui visait une zone non couverte (`calculateExtension`) n'a jamais compilé.

**Pourquoi des mutants survivent aux tests générés :**
- `if ((ch1 | ch2 | ...) < 0)` → `<= 0` : aucun test ne lit **des octets tous nuls**. L'original renvoie 0, le mutant lève une exception à tort.
- `ch1 | ch2` → `ch1 & ch2` : les tests de fin de flux mettent toujours le `-1` **en dernière position**. Le mutant n'est détectable que si l'octet manquant est absorbé par le `&`, donc à une autre position.
- Les surcharges sans décalage (`getIntLE(byte[])`, `getUShortBE(byte[])`…) et `readUShortLE`, `readIntBE` n'ont pas été ciblées.

---

## 7. Tests ajoutés à la main

Fichiers : [`EndianUtilsManualTest.java`](tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java) et [`FilenameUtilsManualTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsManualTest.java). Ils tuent **91 mutants** de plus. PIT attribue chaque mutant au premier test qui le tue.

### EndianUtilsManualTest

**`testReadAllZeroBytesReturnsZero`** (9 mutants)
- *Intention :* vérifier que les 9 méthodes `readUShortLE/BE`, `readUIntLE/BE`, `readIntLE/BE/ME` et `readLongLE/BE` acceptent un flux d'octets nuls.
- *Données :* `00 00`, `00 00 00 00`, ou 8 × `00`. C'est la **valeur limite** de la condition `(ch1 | ch2 | …) < 0` : le OU des octets vaut exactement 0.
- *Oracle :* un flux composé d'octets valides valant 0 encode l'entier 0. Le résultat attendu est donc `0`, sans exception. Le mutant `<= 0` lève `BufferUnderrunException`, ce qui fait échouer le test.

**`testReadDetectsMissingByteAtEveryPosition`** (25 mutants)
- *Intention :* vérifier que la fin de flux est détectée **quelle que soit la position** de l'octet manquant.
- *Données :* un flux simulé (`InputStream` anonyme) qui renvoie `0x11` partout sauf `-1` à la position *k*, pour chaque *k* de 0 à n − 1, et pour toutes les méthodes `read*`. La valeur `0x11` est non nulle, pour que seul le `-1` rende la condition négative.
- *Oracle :* le contrat de `read*` est de lever `EndianUtils.BufferUnderrunException` si un octet manque (`read()` renvoie -1). Les mutants `|` → `&` « absorbent » le -1 à certaines positions (`-1 & 0x11 = 0x11`), et ne lèvent alors plus d'exception.
- *Remarque :* un vrai flux ne renvoie jamais un octet après `-1`. À part pour la dernière position, ces mutants sont donc pratiquement équivalents pour des flux réels. Le flux simulé permet quand même de vérifier la logique de la condition.

**`testReadUntestedStreamMethods`** (14 mutants)
- *Intention :* tester les méthodes jamais appelées, `readUShortLE`, `readIntBE`, `readShortLE` et `readShortBE`.
- *Données :* `34 12` (ordre inversé), `FF FE` (octet de poids fort à 1), `80 00 00 01` (bit de signe à 1) et `FE FF`.
- *Oracle :* calcul à la main selon l'endianness. Par exemple, en little-endian, `34 12` donne `0x1234`. En unsigned, `FF FE` donne `0xFEFF`, et non une valeur négative. Interprété comme `short` signé, `FFFE` vaut −2. Le bit de signe à 1 donne `0x80000001`, un `int` négatif.

**`testArrayMethodsWithAndWithoutOffset`** (15 mutants)
- *Intention :* tester les surcharges `get*(byte[])` sans décalage, `getUShortLE` avec décalage et `getUIntBE`.
- *Données :* des octets à `0xFE`, `0xFF` ou `0xF0` pour vérifier le masque `& 0xFF` (sans lui, l'octet signé `-2` contaminerait le résultat), et un décalage de 1 avec un octet parasite en position 0.
- *Oracle :* `getUShortLE({FE, FF}) = 0xFFFE` ; `getUIntBE({FF, FF, FF, F0}) = 4294967280` (même valeur de référence que dans les tests originaux de `readUIntBE`) ; la surcharge sans décalage doit donner le même résultat qu'avec un décalage de 0.

**`testReadUE7ZeroByteAfterContinuation`** (2 mutants)
- *Intention :* format UE7, où le bit 7 de chaque octet indique si le nombre continue. Le dernier octet vaut 0.
- *Données :* `81 00`, avec un octet de continuation de valeur 1 puis un octet final **nul**. Le zéro est la limite des conditions `i >= 0` et `i < 0`.
- *Oracle :* (1 << 7) + 0 = **128**. Le mutant `i > 0` arrête la lecture trop tôt et renvoie 1. Le mutant `i <= 0` lève une `IOException`.

**`testReadUE7StopsAfterSixBytes`** (2 mutants)
- *Intention :* vérifier la limite `max = 6` octets lus.
- *Données :* 7 octets `0x81`, puis `0x01`.
- *Oracle :* la lecture s'arrête après 6 octets de valeur 1, donc v = 1 + 128 + 128² + … + 128⁵ = (2⁴² − 1) / 127 = **34 630 287 489**. Les mutants `read++ <= max` et `read--` lisent un 7ᵉ octet et renvoient 4 432 676 798 593.

### FilenameUtilsManualTest

**`testSuffixLengthBoundary`** (1 mutant)
- *Intention :* limite de longueur des extensions dans `getSuffixFromPath` (`n.length() - i < 6`).
- *Données :* `file.abcd` (point + 4 caractères = 5) et `file.abcde` (point + 5 caractères = 6), de part et d'autre de la limite.
- *Oracle :* la Javadoc indique qu'une extension doit faire au plus 5 caractères, point inclus. Donc `".abcd"`, puis `""`.

**`testEmbeddedNameFallbackOrder`** (7 mutants)
- *Intention :* `getSanitizedEmbeddedFileName` doit trouver le nom dans **chacune** des métadonnées de repli, quand c'est la seule présente.
- *Données :* une seule clé à la fois parmi `ORIGINAL_RESOURCE_NAME`, `INTERNAL_PATH`, `EMBEDDED_RELATIONSHIP_ID` et `EMBEDDED_RESOURCE_PATH`. Ce sont des noms simples et valides, pour isoler la logique de repli.
- *Oracle :* le code de `getEmbeddedName` consulte ces clés dans l'ordre et renvoie la première non vide. Le nom attendu est donc la valeur fournie, réduite au dernier segment (`/a/b/internal.doc` → `internal.doc`).

**`testEmbeddedPathFallbackOrder`** (4 mutants)
- *Intention, données et oracle :* même principe pour `getSanitizedEmbeddedFilePath` et `getEmbeddedPath`. Le chemin relatif est conservé : `dir/sub/file.txt` → `dir/sub/file.txt`.

**`testCalculateExtension`** (2 mutants)
- *Intention :* tester `calculateExtension`, qu'aucun test original n'appelle directement, et que ChatUniTest n'a pas réussi à tester.
- *Données :* `image/png` (type connu), aucun type (aucun `Content-Type`), et `application/x-ift3913-inconnu` (type inventé).
- *Oracle :* la Javadoc et le code donnent l'extension du registre MIME de Tika (`.png`) ; la valeur par défaut si le type est absent ; `.bin` si le type n'a pas d'extension connue.

**`testDegenerateNamesReturnNull`** (1 mutant)
- *Intention :* les noms inutilisables doivent donner `null`.
- *Données :* `.pdf` (une extension seule), `"\u0000"` (le caractère nul, remplacé par un espace, ce qui donne un chemin vide) et `a/.` (dernier segment `.`).
- *Oracle :* la Javadoc et le code renvoient `null` quand aucun nom de fichier sûr ne peut être construit. Seul le mutant de la ligne 232 (chemin vide) a été tué. Les deux autres entrées renvoient `null` par une autre branche, donc les mutants des lignes 174 et 250 restent non couverts.

**`testNameExactlyMaxLength`** (2 mutants)
- *Intention :* limite de troncature `namePart.length() > maxLength`.
- *Données :* nom `abcde.txt` avec `maxLength = 5`. La partie nom fait **exactement** la longueur maximale.
- *Oracle :* la condition est stricte (`>`), donc un nom de longueur égale n'est pas tronqué, et on attend `abcde.txt`. Le mutant `>=` tente de tronquer et plante (`substring` négatif).

**`testPathExactlyMaxLength`** (7 mutants)
- *Intention :* même limite pour `getSanitizedEmbeddedFilePath`, sur la longueur du chemin complet puis sur celle de la partie nom.
- *Données :* `dir/file.txt` (12 caractères, avec `maxLength = 12`) et `dir/abcdefgh.txt` (partie nom de 8 caractères, avec `maxLength = 8`).
- *Oracle :* chemin de longueur égale conservé (`dir/file.txt`) ; chemin trop long, mais nom de longueur égale, donc on renvoie le nom seul non tronqué (`abcdefgh.txt`).

### Mutants restants (20) et justification
- **`EndianUtils` L362 et L388** (`int b3 = data[i++] & 0xFF;`) : l'incrément du **dernier** octet lu n'est plus utilisé ensuite. Ce sont des mutants **équivalents**, impossibles à tuer.
- **`FilenameUtils` L156, L215, L320** (`prefixLength > 0` → `>= 0` ou négation) : avec `prefixLength = 0`, `substring(0)` renvoie la même chaîne. Ils sont **équivalents**, ou sans effet observable sur le nom final.
- **`getPrefixLength` L323–324** : la branche `"C:"` est **inatteignable**, car `commons-io` renvoie déjà 2 pour `"C:"` à la ligne précédente. C'est du code mort défensif.
- **`resolveWithin` L305 et L308** : ils dépendent de l'état du système de fichiers (liens symboliques réels). Nous ne les avons pas testés, pour éviter des tests dépendants de l'environnement.
- **L174, L185, L250, L259** (`return null` défensifs) : des branches difficiles à atteindre, car les cas limites sont généralement interceptés plus tôt.

---

## 8. Exécution dans la GitHub Action

Le workflow [`.github/workflows/tache2.yml`](.github/workflows/tache2.yml) s'exécute à chaque `push` et `pull_request` :
1. installation de Java 17 (Temurin) ;
2. compilation de `tika-core` et de ses modules parents (`mvn -pl tika-core -am install -DskipTests`) ;
3. exécution de **tous les tests de `tika-core`** (originaux, générés et manuels) ;
4. analyse de mutation PIT sur les deux classes ;
5. publication des rapports PIT et JaCoCo comme artefacts.

La génération ChatUniTest n'est **pas** relancée dans la CI, car elle exige un modèle local (Ollama). Les tests générés sont commités dans `src/test/java`.

Les workflows d'origine d'Apache Tika (builds complets multi-JDK, Docker) sont désactivés dans l'onglet *Actions* du fork, car ils ne concernent pas ce travail.

**Lien vers une exécution réussie :** *(à compléter après le premier push)*

Résultat de cette exécution (Ubuntu, Java 17) :
- étape « Executer tous les tests » : `Tests run: 814, Failures: 0, Errors: 0, Skipped: 2` puis `BUILD SUCCESS` ;
- étape PIT : `Generated 321 mutations Killed 307 (96%)`, `Test strength 97%`, puis `BUILD SUCCESS`.

Ces chiffres diffèrent légèrement de nos mesures locales (Windows, Java 25 : 322 mutants, 302 tués, 94 %). Le bytecode compilé n'est pas exactement le même selon la version de Java, d'où un mutant de différence. De plus, certains mutants de `FilenameUtils` liés aux chemins (`resolveWithin`, préfixes Windows) se comportent différemment sous Linux. Les tableaux des sections 5 à 7 reprennent les mesures locales, dont les rapports complets sont dans `tache2-rapports/`.

---

## 9. Reproduire

```bash
# compiler
mvn -pl tika-core -am install -DskipTests
# tous les tests + couverture JaCoCo (tika-core/target/site/jacoco)
mvn -pl tika-core test
# mutation (tika-core/target/pit-reports)
mvn -pl tika-core -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage
# génération (Ollama doit tourner, avec l'alias codeqwen:v1.5-chat)
mvn -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method -DselectMethod=EndianUtils#readIntLE
```

## 10. Fichiers modifiés ou ajoutés

| Fichier | Changement |
|---|---|
| `tika-core/pom.xml` | dépendances de test Mockito ; profil `tache2` (ChatUniTest + PIT) |
| `tika-core/src/test/java/org/apache/tika/io/*_Test.java` | 14 fichiers de tests générés (corrigés) |
| `tika-core/src/test/java/org/apache/tika/io/*ManualTest.java` | 2 fichiers de tests écrits à la main |
| `tika-core/chatunitest-tests/` | sortie brute de ChatUniTest |
| `.github/workflows/tache2.yml` | GitHub Action |
| `tache2-rapports/` | rapports JaCoCo/PIT, journaux, tests bruts, erreurs de compilation, script de génération |
| `README.md` / `README-TIKA.md` | ce rapport / README d'origine de Tika |

Aucun fichier de code source de Tika (`src/main`) ni aucun test original n'a été modifié.

---

## 11. Déclaration d'utilisation de l'IA générative

Conformément aux [directives de l'Université de Montréal](https://boite-outils.bib.umontreal.ca/c.php?g=743753&p=5377614) :

- **ChatUniTest + qwen2.5-coder:7b (Ollama, local)** : génération automatique des tests `*_Test.java`. C'est l'objet même du travail demandé.
