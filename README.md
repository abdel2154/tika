# IFT3913 – Tâche 2 : augmentation d'une suite de tests avec ChatUniTest et PIT

**Binôme :** Messaad Abdelmouhcine (`abdel2154`) · Cherir Roustom Abdeldjalel (`R0us24`)
**Cas d'étude :** Apache Tika 4.0.0-SNAPSHOT (fork de [umontreal-diro/tika](https://github.com/umontreal-diro/tika)), module `tika-core`
**Classes testées :** `org.apache.tika.io.EndianUtils` et `org.apache.tika.io.FilenameUtils`

> Le README original d'Apache Tika a été déplacé dans [README-TIKA.md](README-TIKA.md).

## Résumé des résultats

Toutes les mesures ci-dessous ont été refaites d'un seul coup, dans le même environnement que la GitHub Action (Java 17), avec le script [`tache2-rapports/mesures-java17/mesures.sh`](tache2-rapports/mesures-java17/mesures.sh). Elles se font en 4 étapes, en ajoutant les tests au fur et à mesure.

| Étape (tests exécutés) | EndianUtils | FilenameUtils | Total |
|---|---|---|---|
| 1. Tests originaux de Tika | 38/206 (**18 %**) | 73/115 (**63 %**) | 111/321 (35 %) |
| 2. + tests ChatUniTest, passe 1 (14 méthodes) | 137/206 (**67 %**) | 73/115 (**63 %**) | 210/321 (65 %) |
| 3. + tests ChatUniTest, passe 2 (17 autres méthodes) | 168/206 (**82 %**) | 78/115 (**68 %**) | 246/321 (77 %) |
| 4. + tests écrits à la main | 204/206 (**99 %**) | 106/115 (**92 %**) | 310/321 (**97 %**) |

*Score de mutation PIT (mutants tués / mutants générés).*

| Couverture JaCoCo | EndianUtils | FilenameUtils |
|---|---|---|
| Lignes, étape 1 → 4 | 31/121 (26 %) → **121/121 (100 %)** | 153/175 (87 %) → **171/175 (98 %)** |
| Branches, étape 1 → 4 | 10/28 → **28/28** | 82/110 → **104/110** |
| Méthodes, étape 1 → 4 | 4/32 → **32/32** | 13/14 → **14/14** |

- **128 tests générés** par ChatUniTest (36 fichiers, 30 méthodes publiques ciblées, soit toutes celles des deux classes), intégrés après **106 corrections manuelles**, toutes marquées `// CORRECTION n` dans le code.
- **18 tests écrits à la main** (2 fichiers).
- Les 895 tests du module `tika-core` passent, localement et dans la GitHub Action [`tache2.yml`](.github/workflows/tache2.yml).
- Les 11 mutants restants sont tous justifiés (section 7) : mutants équivalents, code mort, ou un mutant qui n'est tuable qu'avec un lien symbolique (le test correspondant s'exécute sous Linux).
- En chemin, nous avons trouvé **deux défauts dans Tika** et un **écart entre la Javadoc et le code** (section 4.3).

---

## 1. Choix des classes

Nous avons choisi le module `tika-core` parmi les 5 modules autorisés. Il est léger, compile vite et ne demande pas de fichiers de test lourds (PDF, Office, OCR). Dans ce module, nous avons cherché des classes **déjà testées**, mais **mal couvertes**, et avec des **mutants vivants**.

### `EndianUtils`
Classe utilitaire qui lit des entiers (16, 32 et 64 bits, signés ou non, little/big/middle-endian) dans un flux ou un tableau d'octets. Elle sert aux parseurs de formats binaires.

- La classe de test `EndianUtilsTest` existe, mais ne contient que **4 tests**, sur `readUE7`, `readUIntLE`, `readUIntBE` et `readIntME`.
- JaCoCo (avant) : **31 lignes sur 121 couvertes (26 %)**, **4 méthodes sur 32**, 10 branches sur 28.
- PIT (avant) : **206 mutants, 38 tués (18 %)**, 14 survivants et **154 non couverts**.
- Ce sont des calculs purs sur des bits (`<<`, `&`, `|`, `+`), donc beaucoup de mutants arithmétiques. C'est idéal pour évaluer la qualité des oracles.

### `FilenameUtils`
Classe qui nettoie des noms et chemins de fichiers (caractères réservés, chemins Windows, attaques « zip slip ») et calcule des extensions.

- La classe de test `FilenameUtilsTest` contient **10 tests**.
- JaCoCo (avant) : **153 lignes sur 175 (87 %)**, 82 branches sur 110 (75 %). La méthode publique `calculateExtension` n'est jamais appelée directement par un test.
- PIT (avant) : **115 mutants, 73 tués (63 %)**, **20 survivants** et 22 non couverts. Les survivants se trouvent notamment dans les conditions limites (`>` / `>=`) et dans l'ordre de repli des métadonnées (`getEmbeddedName`, `getEmbeddedPath`).

Rapports « avant » : [`mesures-java17/jacoco-1-originaux/`](tache2-rapports/mesures-java17/jacoco-1-originaux/) et [`mesures-java17/pit-1-originaux/`](tache2-rapports/mesures-java17/pit-1-originaux/). Ce sont des fichiers HTML, à télécharger et ouvrir dans un navigateur ; `mutations.csv` et `jacoco.csv` sont lisibles directement.

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
3. **Temps de génération.** Sur CPU, une génération de toute la classe `EndianUtils` en mode classe (32 méthodes × 2 tests, 3 rondes de réparation) était estimée à plus de 2 heures. Ce premier essai a été arrêté après 3 méthodes, qui échouaient toutes à la compilation (erreurs conservées dans [`chatunitest-essai1-erreurs/`](tache2-rapports/chatunitest-essai1-erreurs/)). Nous avons donc travaillé **méthode par méthode** (but `chatunitest:method`), avec **1 test par méthode et 2 rondes de réparation**, en deux passes :
   - **passe 1** (3 octobre, environ 1 h, de 19 h 34 à 20 h 31) : 14 méthodes choisies parmi les moins couvertes (10 de `EndianUtils`, 4 de `FilenameUtils`), avec le script [`generer.ps1`](tache2-rapports/generer.ps1) ;
   - **passe 2** (7 octobre, environ 1 h, de 1 h 45 à 2 h 48) : les **17 autres méthodes publiques** (13 de `EndianUtils`, et `getSanitizedEmbeddedFileName`, `getSanitizedEmbeddedFilePath`, `resolveWithin` et un nouvel essai de `calculateExtension` dans `FilenameUtils`), avec le script [`generer-suite.ps1`](tache2-rapports/generer-suite.ps1).

   Au total, **toutes les méthodes publiques des deux classes** ont été soumises à ChatUniTest : 30 méthodes (23 + 7), soit 31 générations, car `calculateExtension` a été tentée deux fois. Les méthodes de `FilenameUtils` sont les plus lentes (jusqu'à 13 minutes), car leurs prompts sont longs.
4. **Plantage du GPU (passe 2).** Sur le second ordinateur, Ollama tentait d'utiliser une carte NVIDIA dont le pilote est trop ancien. Le serveur du modèle plantait (*« CUDA error: device kernel image is invalid »*) et ChatUniTest recevait des erreurs HTTP 500. Nous avons relancé Ollama **en mode CPU uniquement** (`CUDA_VISIBLE_DEVICES=-1`, `OLLAMA_LLM_LIBRARY=cpu`), comme sur le premier ordinateur, avec une fenêtre de contexte de 8 192 jetons pour que les prompts de ChatUniTest (jusqu'à environ 11 000 jetons) soient moins tronqués.
5. **Délai d'attente du plugin.** Pour `getSanitizedEmbeddedFileName`, la ronde de réparation a échoué avec `SocketTimeoutException` : sur CPU, le modèle met plus de temps à répondre que le délai HTTP du plugin, qui n'est pas configurable. La dernière tentative valide a été conservée.

```bash
mvn -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method -DselectMethod=EndianUtils#readIntLE
```

---

## 3. Résultat de la génération

### Où sont les tests générés ?
- **Sortie brute de ChatUniTest :** `tika-core/chatunitest-tests/org/apache/tika/io/`. ChatUniTest n'y écrit que les tests qu'il considère réussis. Des copies non modifiées se trouvent dans [`tache2-rapports/chatunitest-bruts/`](tache2-rapports/chatunitest-bruts/) (passe 1) et [`chatunitest-bruts-suite/`](tache2-rapports/chatunitest-bruts-suite/) (passe 2).
- **Tentatives que ChatUniTest a rejetées** (code et message d'erreur du compilateur ou de JUnit) : [`chatunitest-erreurs/`](tache2-rapports/chatunitest-erreurs/) (passe 1), [`chatunitest-erreurs-suite/`](tache2-rapports/chatunitest-erreurs-suite/) (passe 2) et [`chatunitest-essai1-erreurs/`](tache2-rapports/chatunitest-essai1-erreurs/) (premier essai en mode classe, interrompu).
- **Version intégrée au projet (après corrections) :** `tika-core/src/test/java/org/apache/tika/io/*_Test.java` (**36 fichiers, 128 tests**). Chaque fichier commence par un commentaire qui indique sa provenance, et chaque correction est signalée par un commentaire `// CORRECTION n` qui l'explique.
- Journaux de génération : [`logs/`](tache2-rapports/logs/) (passe 1) et [`logs-suite/`](tache2-rapports/logs-suite/) (passe 2).

Quand ChatUniTest rejetait une méthode, nous sommes partis de sa **dernière tentative** (`..._Error_1.txt`) et l'avons réparée à la main. Pour compter les erreurs de compilation de façon honnête, nous avons compilé chaque fichier brut avec `javac`, **avant** que Spotless (qui reformate le code et retire les imports inutilisés à chaque build de Tika) ne le modifie.

### Compilent-ils et s'exécutent-ils sans intervention ? **Non.**

**Passe 1 (14 méthodes) :**

| Méthode ciblée | Selon ChatUniTest | Réalité dans le projet | Corrections |
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
| **Total passe 1** | 5 échecs, 9 « succès » | 14 fichiers, 52 tests | **48** |

**Passe 2 (17 méthodes) :**

| Méthode ciblée | Selon ChatUniTest | Réalité dans le projet | Tests | Corrections |
|---|---|---|---|---|
| `EndianUtils.readShortLE` | échec de compilation (2 rondes) | réparé | 2 | 2 |
| `EndianUtils.readShortBE` | échec de compilation | réparé | 3 | 3 |
| `EndianUtils.readUShortLE` | échec de compilation | réparé | 8 | 3 |
| `EndianUtils.readUIntLE` | échec de compilation | réparé, **1 assertion fausse** | 3 | 5 |
| `EndianUtils.readUIntBE` | échec de compilation | réparé, **1 assertion fausse** | 2 | 5 |
| `EndianUtils.readIntBE` | échec de compilation | réparé | 2 | 2 |
| `EndianUtils.readIntME` | échec de compilation | réparé | 2 | 2 |
| `EndianUtils.readUE7` | « compile and execute successfully » (ronde 1) | **3 assertions fausses** | 1 | 3 |
| `EndianUtils.getShortLE` (2 surcharges) | « compile and execute successfully » | **2 tests faux** (mock de `byte[]`, exception attendue à tort) | 5 | 3 |
| `EndianUtils.getUShortLE` (2 surcharges) | succès | **1 assertion fausse** | 3 | 1 |
| `EndianUtils.getUShortBE` (2 surcharges) | succès | **1 assertion fausse** | 5 | 1 |
| `EndianUtils.getIntLE` (2 surcharges) | succès (après 1 ronde) | **2 assertions fausses** | 4 | 2 |
| `EndianUtils.getUIntBE` (2 surcharges) | « generated successfully » | **1 assertion fausse** | 2 | 1 |
| `FilenameUtils.getSanitizedEmbeddedFileName` | échec à l'exécution (2 rondes, *timeout*) | réparé | 14 | 9 |
| `FilenameUtils.getSanitizedEmbeddedFilePath` | échec de compilation | réparé, **6 assertions fausses** | 9 | 7 |
| `FilenameUtils.resolveWithin` | « generated successfully » | passe | 1 | 0 |
| `FilenameUtils.calculateExtension` | échec à l'exécution (2 rondes) | réparé | 10 | 9 |
| **Total passe 2** | 10 échecs, 7 « succès » (12 fichiers) | 22 fichiers, **76 tests** | 76 | **58** |

*Tous les fichiers* : aucun ne compile sans **Mockito**, que `tika-core` n'utilisait pas. Nous avons ajouté `mockito-core` et `mockito-junit-jupiter` (scope `test`) au `pom.xml` (1 correction, comptée à part).

**Bilan :**
- Sur 31 générations, ChatUniTest déclare **16 succès** et **15 échecs**. En réalité, **aucun fichier ne compile tel quel** (Mockito), et parmi les 22 fichiers « réussis » selon l'outil, **12 contiennent des assertions fausses** : 21 tests échouent dès qu'on les exécute avec Maven Surefire (13 en passe 1, 8 en passe 2). On ne peut donc pas se fier au verdict de l'outil.
- Seuls **10 fichiers sur 36** passent sans autre retouche que Mockito.
- En tout, **106 corrections manuelles** (48 + 58), plus la dépendance Mockito. En passe 2, toutes les tentatives rejetées ont pu être réparées, y compris `calculateExtension`, abandonné en passe 1.
- Les rondes de réparation automatique servent peu : seules 3 méthodes sont passées d'un échec à un « succès » grâce à elles (`getUIntLE` en passe 1, `readUE7` et `getIntLE` en passe 2), et le test « réparé » de `readUE7` contenait 3 assertions fausses. Dans les 15 échecs, la ronde de réparation n'a pas corrigé l'erreur principale (par exemple, l'import inventé de `BufferUnderrunException`).

### Types d'erreurs de compilation (code généré par le LLM)
1. **Classes imaginaires ou mal localisées :** `org.apache.tika.io.InputStream`, `InputStreams`, `InputStreamWithPosition`, `org.apache.tika.io.BufferUnderrunException`, `org.apache.tika.exception.BufferUnderrunException`. La vraie classe est la classe imbriquée `EndianUtils.BufferUnderrunException`. Le modèle n'a **jamais** réussi à corriger cette erreur, même quand le message du compilateur la lui indiquait. C'est la cause d'échec la plus fréquente : elle apparaît dans les 7 méthodes `read*` rejetées en passe 2.
2. **Mauvaise exception :** `java.nio.BufferUnderflowException` (nom proche, mais autre classe).
3. **Exception vérifiée non déclarée :** `BufferUnderrunException` hérite de `TikaException`. Les méthodes de test ne déclaraient que `throws IOException`.
4. **Variable non effectivement finale** utilisée dans une lambda `assertThrows`.
5. **Appel ambigu :** `metadata.set(Property, null)` est ambigu entre `set(Property, Date)` et `set(Property, Calendar)` (dans les deux passes).
6. **Mauvaise signature :** `verify(stream, never()).read(anyInt())`, alors qu'`InputStream` n'a pas de méthode `read(int)`.

Et à l'exécution :

7. **Usage incorrect de Mockito :** mock d'un tableau `byte[]` (impossible, et `when(data[0])` n'est pas un appel de méthode) ; stubs inutiles ou incompatibles refusés par le mode strict de Mockito (`UnnecessaryStubbingException`, `PotentialStubbingProblem`).

---

## 4. Critique des oracles : IA contre tests écrits à la main

### 4.1 Ce qui est bien dans les tests générés
- **Oracles spécifiques** dans la majorité des cas : valeurs exactes avec `assertEquals`, par exemple `assertEquals(0x01020304, EndianUtils.getIntBE(data, 0))`. Les octets distincts (`01 02 03 04`) permettent de détecter une inversion d'ordre ou une erreur de décalage. C'est ce qui tue la plupart des mutants arithmétiques.
- **Tests de robustesse** que les tests originaux n'avaient pas : décalage négatif, décalage hors du tableau, tableau vide, tableau ou flux `null`.
- Quelques oracles **non triviaux et justes** : `readIntME({01 02 03 04}) = 0x02010403` (ordre « middle-endian », peu intuitif) ; `readUShortLE` testé avec des valeurs limites pertinentes (`FF FF`, `7F FF`, `00 00`, flux vide, flux `null`).

### 4.2 Ce qui est mauvais
1. **Oracles faux par incompréhension de la spécification.** Le modèle « devine » le résultat au lieu de le déduire :
   - **endianness :** pour `getShortBE({0x00, 0x01})`, il attend `0x0100` ; le bon résultat big-endian est `0x0001`. Pour `readLongLE(1..8)` et `getIntLE(1..4)`, il attend l'ordre big-endian (`0x01020304`) au lieu de `0x04030201` ;
   - **décalages :** `getUShortLE({01 02 03 04}, 1)` doit lire `02 03`, soit `0x0302 = 770`, mais il attend `513`, la valeur du décalage 0. Même erreur pour `getUShortBE` (`514` au lieu de `515`) et `getUIntBE` (il lit 4 octets nuls et attend `1`) ;
   - **format UE7 ignoré :** pour `readUE7({01 02 … 08})`, il attend `0x0102030405060708`. Or le bit 7 de chaque octet indique si le nombre continue : `0x01` est donc le dernier octet, et le résultat est `1`. Les 4 cas du test avaient la même valeur attendue ;
   - **`normalize` :** il suppose que l'espace et `/` sont des caractères réservés (`"example%20txt"`), alors que la liste `RESERVED_FILENAME_CHARACTERS` ne les contient pas. Résultat : 24 assertions fausses ;
   - **`getName(".file.txt")` :** il attend `""` (il confond avec le cas `"."` ou `".."`) ;
   - **`getSuffixFromPath("example.docx")` :** il attend `""`, alors que la Javadoc dit qu'une extension alphanumérique de 5 caractères ou moins (point inclus) est acceptée ;
   - **octet `0xFF` :** il croit qu'un octet `-1` signifie « fin de flux » et attend une exception (`readUShortBE`, `readLongBE`, `readUIntLE`), alors que `read()` renvoie `255` pour cet octet. Seule la valeur `-1` retournée par `read()` signale la fin ;
   - **noms de fichiers :** il croit que l'extension par défaut reçoit un point automatiquement (`"txt"` → `"example.txt"`), que l'hôte d'une URL est retiré (`http://example.com/path/to/file.txt` → `path/to/file.txt`), que le chemin complet est gardé quand il dépasse `maxLength` (le code ne garde que le nom), et il se trompe dans la formule de troncature (`maxLength - 3`, au lieu de `maxLength - extension.length() - 3`) ;
   - **`':'` :** il ignore que `':'` est un séparateur de chemin (anciens noms Mac). Dans `example?*<>|":'`, seul le dernier segment `'` est conservé.
2. **Oracles triviaux, vides ou tautologiques :**
   - `ubyteToInt` : `int result = (int) b & 0xFF; assertEquals(result, EndianUtils.ubyteToInt(b));`. L'oracle **recopie l'implémentation**, donc le test vérifie le code avec lui-même ;
   - `getUByte` (décalage invalide) : un `try { ... } catch (ArrayIndexOutOfBoundsException e) {}` **sans `fail()`**. Le test passe que l'exception soit levée ou non ;
   - **tests sans aucune assertion :** `testGetSanitizedEmbeddedFileName_withEmptyMaxLengthInput` (corps vide) et `testGetSanitizedEmbeddedFilePath_withLongExtension` (prépare des données puis s'arrête) ;
   - `getUIntLE` : les tests 2 et 3 sont **identiques**, et aucun ne teste un octet de poids fort à 1. Or c'est justement ce qui distingue un entier *non signé*.
3. **Bruit et doublons :** `@ExtendWith(MockitoExtension.class)` et `@Mock InputStream` jamais utilisés ; tests en double (`getName`, `getSanitizedEmbeddedFileName` : `withNullExtensionInput` et `withNullDefaultExtensionInput`, `getSanitizedEmbeddedFilePath` : `withValidPath` et `withProtocol`) ; méthode publique appelée par réflexion (`getSuffixFromPath`, `getUIntBE`) ; instanciation inutile de la classe utilitaire (`new EndianUtils()`, `new FilenameUtils()`).
4. **Mocks inutiles ou absurdes :** le test de `calculateExtension` (dans les deux passes) « mocke » un `MimeTypes` qui n'est jamais injecté dans la méthode (celle-ci utilise un registre statique). Le test de `resolveWithin` simule un `Path` dont `resolve()` renvoie un chemin extérieur : il teste un scénario impossible avec un vrai système de fichiers, et ne vérifie que le message d'erreur le plus simple.

### 4.3 Quand l'oracle de l'IA révèle un problème dans Tika
Corriger les tests générés oblige à chercher le **vrai** comportement attendu dans le code et la Javadoc. Cela nous a fait découvrir trois problèmes :
- **Javadoc contredite par le code.** La Javadoc de `calculateExtension` dit : *« On parse exception or null value, return the default value »*. Le test généré suit cette documentation : pour un `Content-Type` vide ou inconnu, il attend la valeur par défaut. Mais le code renvoie `".bin"` dans ces deux cas ; seule l'absence de `Content-Type` donne la valeur par défaut. Ici, **l'IA a raison selon la spécification écrite**, et c'est le code qui s'en écarte. Nous avons aligné les tests sur le code (on ne peut pas modifier `src/main`), en le signalant dans les commentaires `// CORRECTION`.
- **Défaut 1 – `getSanitizedEmbeddedFilePath` sans extension.** Pour `http://example.com/path/to/file`, le code renvoie `example.com/path/to/example.com_path_to_file` : quand le nom n'a pas d'extension, la partie nom reprend **tout le chemin** (`namePart = path` au lieu de `fName`), puis le chemin relatif est ajouté devant. Le résultat attendu par l'IA (`path/to/file.txt`) est faux pour l'hôte et l'extension, mais il garde le nom `file` à la fin du chemin : il est plus proche de l'intention du code que le comportement réel. Le test corrigé fige le comportement réel, et le défaut est expliqué dans le commentaire.
- **Défaut 2 – noms commençant par `~`.** Pour un nom de ressource `~` ou `~user`, `commons-io` renvoie une longueur de préfixe **plus grande que le nom** (2 et 6), et `path.substring(prefixLength)` lève une `StringIndexOutOfBoundsException`. Un fichier intégré nommé `~` dans une archive ferait donc échouer l'extraction. Nous l'avons trouvé en analysant un mutant survivant (section 7, `testTildeNameKnownDefect`).

### 4.4 Comparaison avec les tests écrits à la main (Tika)
- Les tests originaux utilisent des **données issues du domaine** : par exemple `0xF0 0xFF 0xFF 0xFF` → `4294967280L`, qui vérifie explicitement le caractère *non signé* de `readUIntLE`. `FilenameUtilsTest` vérifie de vrais chemins malveillants (`../../../`, chemins Windows, protocoles). Le modèle n'a jamais produit ce type de cas.
- Les tests originaux sont **peu nombreux mais ciblés**. Les tests générés sont **nombreux mais redondants** : 128 tests générés tuent 135 mutants (étapes 1 → 3), alors que 18 tests manuels en tuent 64 de plus, parmi les plus difficiles.
- Les tests originaux ne sont pas parfaits non plus : `EndianUtilsTest.testReadUIntBE` contient un **copier-coller erroné**. Sa vérification de l'exception appelle `readUIntLE` au lieu de `readUIntBE`, donc la détection de fin de flux de `readUIntBE` n'était pas testée. Nous ne l'avons pas modifié, pour garder les tests originaux intacts.

---

## 5. Ajout de PIT et analyse de mutation

PIT (`pitest-maven` 1.30.0 avec `pitest-junit5-plugin` 1.2.3) est configuré dans le même profil `tache2`, avec `targetClasses = EndianUtils*, FilenameUtils` et `targetTests = org.apache.tika.io.*`. Mutateurs par défaut.

```bash
mvn -pl tika-core -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage
```

Pour mesurer chaque étape, nous excluons des groupes de tests avec `-DexcludedTestClasses` (PIT) et `-Dtest=!…` (Surefire, pour JaCoCo). Voir [`mesures.sh`](tache2-rapports/mesures-java17/mesures.sh).

| Étape | Tests | EndianUtils | FilenameUtils | Total |
|---|---|---|---|---|
| 1. Tests originaux | `EndianUtilsTest`, `FilenameUtilsTest`, autres tests de `io` | 38/206 = **18 %** | 73/115 = **63 %** | 111/321 = 35 % |
| 2. + génération passe 1 | + 14 fichiers `*_Test` | 137/206 = **67 %** | 73/115 = **63 %** | 210/321 = 65 % |
| 3. + génération passe 2 | + 22 fichiers `*_Test` | 168/206 = **82 %** | 78/115 = **68 %** | 246/321 = 77 % |
| 4. + tests manuels | + `EndianUtilsManualTest`, `FilenameUtilsManualTest` | 204/206 = **99 %** | 106/115 = **92 %** | 310/321 = **97 %** |

Rapports PIT : [`pit-1-originaux/`](tache2-rapports/mesures-java17/pit-1-originaux/), [`pit-2-generation-passe1/`](tache2-rapports/mesures-java17/pit-2-generation-passe1/), [`pit-3-generation-passe2/`](tache2-rapports/mesures-java17/pit-3-generation-passe2/), [`pit-4-final/`](tache2-rapports/mesures-java17/pit-4-final/). Rapports JaCoCo correspondants : `jacoco-1-…` à `jacoco-4-…` dans le même dossier. Chaque dossier contient `index.html` et un fichier CSV.

**Les tests générés ne détectent pas tous les mutants :** après l'étape 3, il reste 35 mutants vivants et 3 non couverts dans `EndianUtils`, et 18 vivants et 19 non couverts dans `FilenameUtils`.

> **Note sur les anciennes mesures.** Les dossiers `tache2-rapports/jacoco-avant`, `jacoco-final`, `pit-avant`, `pit-apres-generation` et `pit-final` contiennent les mesures de la passe 1, faites sous Java 25 (322 mutants au lieu de 321, 126 et 184 lignes au lieu de 121 et 175, car le bytecode diffère selon la version de Java). Nous les gardons pour l'historique, mais tous les chiffres de ce rapport viennent de `mesures-java17/`, mesurés d'un seul coup dans un environnement identique à la CI.

---

## 6. Mutants détectés par les tests générés, et pourquoi

### Passe 1 : 99 nouveaux mutants tués, tous dans `EndianUtils`

| Méthode | Mutants tués | Test générateur |
|---|---|---|
| `readLongBE` | 17 | `EndianUtils_readLongBE_10_0_Test` |
| `readLongLE` | 17 | `EndianUtils_readLongLE_9_0_Test` |
| `getIntBE` | 14 | `EndianUtils_getIntBE_23_0_Test` |
| `getIntLE` | 14 | `EndianUtils_getUIntLE_24_0_Test` (via `getUIntLE`) |
| `readIntLE` | 9 | `EndianUtils_readIntLE_6_0_Test` |
| `getLongLE` | 8 | `EndianUtils_getLongLE_28_0_Test` |
| `getUShortBE` | 6 | `EndianUtils_getShortBE_16_0_Test` (via `getShortBE`) |
| `readUShortBE` | 5 | `EndianUtils_readUShortBE_3_0_Test` |
| `getUIntLE` | 3 | `EndianUtils_getUIntLE_24_0_Test` |
| `ubyteToInt`, `getShortBE`, `getUByte` | 2 chacun | tests correspondants |

### Passe 2 : 36 nouveaux mutants tués (31 dans `EndianUtils`, 5 dans `FilenameUtils`)

| Méthode | Mutants tués | Test générateur |
|---|---|---|
| `readIntBE` | 8 | `EndianUtils_readIntBE_7_0_Test` |
| `getUShortLE` | 7 | `EndianUtils_getShortLE_13_0_Test` (via `getShortLE`) |
| `readUShortLE` | 6 | `EndianUtils_readUShortLE_2_0_Test` |
| `getUIntBE` | 2 | `EndianUtils_getUIntBE_26_0_Test` |
| `readUE7` | 2 | `EndianUtils_readUE7_11_0_Test` (après correction) |
| `getIntLE`, `getShortLE`, `getUShortBE`, `readShortBE`, `readShortLE`, `readUIntBE` | 1 chacun | tests correspondants |
| `calculateExtension`, `lookupExtension` | 1 chacun | `FilenameUtils_calculateExtension_10_0_Test` |
| `getEmbeddedName`, `getEmbeddedPath`, `getSanitizedEmbeddedFilePath` | 1 chacun | tests de `getSanitizedEmbeddedFileName` / `FilePath` |

La passe 2 tue moins de mutants que la passe 1, alors qu'elle ajoute plus de tests : plusieurs méthodes visées (`readUIntLE`, `readUIntBE`, `readIntME`, `readUE7`) étaient déjà couvertes par les tests originaux, et les méthodes `get*` délèguent souvent à une méthode déjà testée en passe 1.

### Par type de mutateur (passes 1 + 2)
- **MathMutator (89 mutants)** : PIT remplace `<<` par `>>`, `+` par `-`, `&` par `|`, etc., dans des expressions comme `(ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4`. Ils sont **tués parce que les tests utilisent des octets tous différents** (`01 02 03 04 ...`) et comparent la valeur exacte avec `assertEquals`. Toute modification d'un décalage ou d'un opérateur déplace ou altère un octet, et l'assertion échoue.
- **PrimitiveReturnsMutator (24)** : `return x` devient `return 0`. Tués parce que la valeur attendue n'est jamais 0.
- **NegateConditionalsMutator (9)** : `if ((ch1 | ... ) < 0) throw` devient `>= 0`. Une entrée **valide** lève alors une exception, et le test, qui attend une valeur, échoue.
- **IncrementsMutator (7)** : `data[i++]` devient `data[i--]`. Tués parce que l'octet lu ensuite n'est plus le bon, et la valeur calculée change.
- **EmptyObjectReturnValsMutator (3)** : une chaîne renvoyée devient `""` dans `calculateExtension`, `lookupExtension` et `getSanitizedEmbeddedFilePath`. Tués parce que les tests comparent la chaîne exacte (`".bin"`, `"file.txt"`).
- **ConditionalsBoundaryMutator (3)** : la borne de la boucle de `getLongLE`, la limite de 6 octets de `readUE7` (grâce à notre correction 2 du test généré, qui lit 8 octets de continuation) et le cas `00 00` de `readUShortLE`.

### Pourquoi des mutants survivent aux tests générés (étape 3)
- **Conditions de fin de flux (31 mutants)** dans `if ((ch1 | ch2 | ...) < 0)` :
  - `< 0` → `<= 0` (8 mutants) : aucun test généré ne lit **des octets tous nuls** (sauf pour `readUShortLE`). L'original renvoie 0, le mutant lève une exception à tort ;
  - `|` → `&` (23 mutants) : les tests de fin de flux mettent toujours le `-1` **en dernière position**. Le mutant n'est détectable que si l'octet manquant est « absorbé » par le `&`, donc à une autre position.
- **`readUE7` (2 mutants)** : `>= 0` → `> 0` et `< 0` → `<= 0`. Aucun test généré ne contient d'octet final **nul**.
- **Surcharges sans décalage (3 mutants non couverts)** : `getIntBE(byte[])`, `getShortLE(byte[])` et `getUIntBE(byte[])`. Les tests générés n'ont appelé que la version avec décalage.
- **Ordre de repli des métadonnées (14 mutants)** : les tests générés ne remplissent que la première clé consultée (`RESOURCE_NAME_KEY`, `EMBEDDED_RESOURCE_PATH`). Les clés de repli ne sont jamais testées seules.
- **Limites de longueur (4 mutants)** : aucun test généré ne prend un nom ou un chemin de longueur **exactement égale** à la limite.
- **Préfixes de lecteur, `resolveWithin`, retours `null` (le reste)** : entrées particulières jamais essayées par le modèle (voir section 7).

---

## 7. Tests ajoutés à la main

Fichiers : [`EndianUtilsManualTest.java`](tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java) (6 tests) et [`FilenameUtilsManualTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsManualTest.java) (12 tests). Ensemble, ils tuent les **64 mutants** qui survivaient encore à l'étape 3 (36 dans `EndianUtils`, 28 dans `FilenameUtils`).

Le nombre entre parenthèses est le nombre de mutants que PIT attribue au test à l'étape 4. PIT attribue chaque mutant au **premier** test qui le tue ; un test à « 0 » peut donc tuer des mutants déjà attribués à un autre test. Les tests de la première séance ont été écrits après la passe 1 : certains de leurs mutants sont maintenant aussi tués par les tests générés en passe 2.

### EndianUtilsManualTest

**`testReadAllZeroBytesReturnsZero`** (8 mutants)
- *Intention :* vérifier que les 9 méthodes `readUShortLE/BE`, `readUIntLE/BE`, `readIntLE/BE/ME` et `readLongLE/BE` acceptent un flux d'octets nuls.
- *Données :* `00 00`, `00 00 00 00`, ou 8 × `00`. C'est la **valeur limite** de la condition `(ch1 | ch2 | …) < 0` : le OU des octets vaut exactement 0.
- *Oracle :* un flux composé d'octets valides valant 0 encode l'entier 0. Le résultat attendu est donc `0`, sans exception. Le mutant `<= 0` lève `BufferUnderrunException`, ce qui fait échouer le test.

**`testReadDetectsMissingByteAtEveryPosition`** (23 mutants)
- *Intention :* vérifier que la fin de flux est détectée **quelle que soit la position** de l'octet manquant.
- *Données :* un flux simulé (`InputStream` anonyme) qui renvoie `0x11` partout sauf `-1` à la position *k*, pour chaque *k* de 0 à n − 1, et pour toutes les méthodes `read*`. La valeur `0x11` est non nulle, pour que seul le `-1` rende la condition négative.
- *Oracle :* le contrat de `read*` est de lever `EndianUtils.BufferUnderrunException` si un octet manque (`read()` renvoie -1). Les mutants `|` → `&` « absorbent » le -1 à certaines positions (`-1 & 0x11 = 0x11`), et ne lèvent alors plus d'exception.
- *Remarque :* un vrai flux ne renvoie jamais un octet après `-1`. À part pour la dernière position, ces mutants sont donc pratiquement équivalents pour des flux réels. Le flux simulé permet quand même de vérifier la logique de la condition.

**`testArrayMethodsWithAndWithoutOffset`** (3 mutants)
- *Intention :* tester les surcharges `get*(byte[])` **sans décalage**, que les tests générés n'appellent pas, ainsi que `getUShortLE` avec décalage et `getUIntBE`.
- *Données :* des octets à `0xFE`, `0xFF` ou `0xF0` pour vérifier le masque `& 0xFF` (sans lui, l'octet signé `-2` contaminerait le résultat), et un décalage de 1 avec un octet parasite en position 0.
- *Oracle :* `getUShortLE({FE, FF}) = 0xFFFE` ; `getUIntBE({FF, FF, FF, F0}) = 4294967280` (même valeur de référence que dans les tests originaux de `readUIntBE`) ; la surcharge sans décalage doit donner le même résultat qu'avec un décalage de 0.

**`testReadUE7ZeroByteAfterContinuation`** (2 mutants)
- *Intention :* format UE7, où le bit 7 de chaque octet indique si le nombre continue. Le dernier octet vaut 0.
- *Données :* `81 00`, avec un octet de continuation de valeur 1 puis un octet final **nul**. Le zéro est la limite des conditions `i >= 0` et `i < 0`.
- *Oracle :* (1 << 7) + 0 = **128**. Le mutant `i > 0` arrête la lecture trop tôt et renvoie 1. Le mutant `i <= 0` lève une `IOException`.

**`testReadUntestedStreamMethods`** (0 mutant attribué)
- *Intention :* tester `readUShortLE`, `readIntBE`, `readShortLE` et `readShortBE`, qui n'étaient appelées par aucun test avant la passe 2.
- *Données :* `34 12` (ordre inversé), `FF FE` (octet de poids fort à 1), `80 00 00 01` (bit de signe à 1) et `FE FF`.
- *Oracle :* calcul à la main selon l'endianness. Par exemple, en little-endian, `34 12` donne `0x1234`. En unsigned, `FF FE` donne `0xFEFF`, et non une valeur négative. Interprété comme `short` signé, `FFFE` vaut −2. Le bit de signe à 1 donne `0x80000001`, un `int` négatif.
- *Remarque :* écrit après la passe 1, où il tuait 14 mutants. Ces mutants sont maintenant tués par les tests générés de la passe 2. Nous le gardons, car il vérifie des valeurs signées que les tests générés ne vérifient pas.

**`testReadUE7StopsAfterSixBytes`** (0 mutant attribué)
- *Intention :* vérifier la limite `max = 6` octets lus.
- *Données :* 7 octets `0x81`, puis `0x01`.
- *Oracle :* la lecture s'arrête après 6 octets de valeur 1, donc v = 1 + 128 + 128² + … + 128⁵ = (2⁴² − 1) / 127 = **34 630 287 489**. Les mutants `read++ <= max` et `read--` lisent un 7ᵉ octet et renvoient 4 432 676 798 593.
- *Remarque :* ses 2 mutants sont maintenant aussi tués par le test généré de `readUE7`, après notre correction 2 (8 octets de continuation, donc la limite de 6 est atteinte).

### FilenameUtilsManualTest

**`testEmbeddedNameFallbackOrder`** (7 mutants)
- *Intention :* `getSanitizedEmbeddedFileName` doit trouver le nom dans **chacune** des métadonnées de repli, quand c'est la seule présente.
- *Données :* une seule clé à la fois parmi `ORIGINAL_RESOURCE_NAME`, `INTERNAL_PATH`, `EMBEDDED_RELATIONSHIP_ID` et `EMBEDDED_RESOURCE_PATH`. Ce sont des noms simples et valides, pour isoler la logique de repli.
- *Oracle :* le code de `getEmbeddedName` consulte ces clés dans l'ordre et renvoie la première non vide. Le nom attendu est donc la valeur fournie, réduite au dernier segment (`/a/b/internal.doc` → `internal.doc`).

**`testEmbeddedPathFallbackOrder`** (4 mutants)
- *Intention, données et oracle :* même principe pour `getSanitizedEmbeddedFilePath` et `getEmbeddedPath`. Le chemin relatif est conservé : `dir/sub/file.txt` → `dir/sub/file.txt`.

**`testTildeNameKnownDefect`** (4 mutants) — *ajouté en seconde séance*
- *Intention :* documenter le comportement de `getSanitizedEmbeddedFileName` et `getSanitizedEmbeddedFilePath` pour un nom qui ressemble à un répertoire personnel Unix (`~`, `~user`).
- *Données :* `~` et `~user`. Ce sont les seuls noms pour lesquels `commons-io` renvoie une longueur de préfixe **supérieure** à la longueur du nom (2 et 6). Nous les avons trouvés en cherchant une entrée qui distingue « préfixe retiré » et « préfixe non retiré » (mutant `prefixLength > 0` → `<= 0`, ligne 156).
- *Oracle :* **comportement actuel**, vérifié en l'exécutant : `StringIndexOutOfBoundsException`. C'est un **défaut de Tika** (section 4.3) : la Javadoc promet un nom « assaini », pas une exception. Le test fige ce comportement pour tuer le mutant, et son commentaire précise qu'il faudra changer l'oracle quand le défaut sera corrigé. Il tue aussi 3 mutants de `getEmbeddedPath` : un repli modifié ne trouve plus `~user`, donc aucune exception n'est levée.

**`testOnlyRealDriveLettersAreStripped`** (4 mutants) — *ajouté en seconde séance*
- *Intention :* `getPrefixLength` ne doit considérer comme préfixe de lecteur Windows que `X:` avec X entre A et Z.
- *Données :* `A` (une lettre seule, sans `:`), `AB` (deux caractères, sans `:`), `@:` et `[:`. `@` et `[` sont les **voisins immédiats** de l'intervalle `A..Z` dans la table ASCII.
- *Oracle :* aucune de ces chaînes n'est un lecteur (`commons-io` renvoie 0 ou -1), donc rien n'est retiré. `':'` devient `'/'`, le `'/'` final est supprimé, et l'extension par défaut `.bin` est ajoutée : `A.bin`, `AB.bin`, `@.bin`, `[.bin`. Chacun des 4 mutants « condition niée » de la ligne 323 traite une de ces chaînes comme un lecteur : il renvoie `null` ou plante (`charAt(1)` sur `A`).

**`testBlankNamePartReturnsNull`** (2 mutants) — *ajouté en seconde séance*
- *Intention :* un nom dont la partie avant l'extension est vide (après nettoyage) doit donner `null`, dans les deux méthodes.
- *Données :* ` .pdf`, un espace suivi d'une extension valide. C'est le seul type d'entrée qui atteint les `return null` des lignes 174 et 250 : `.pdf` seul est intercepté plus tôt, car l'extension est égale au nom.
- *Oracle :* la partie nom `" "` est blanche (`StringUtils.isBlank`), donc le code renvoie `null`. Les mutants qui renvoient `""` à la place sont détectés.

**`testResolveWithinExistingAndMissingFiles`** (2 mutants) — *ajouté en seconde séance*
- *Intention :* tester la vérification « défense en profondeur » de `resolveWithin`, qui n'a lieu que si les chemins **existent réellement**.
- *Données :* un dossier temporaire réel (`@TempDir`) contenant `inside.txt`, et un nom absent (`absent.txt`). Les tests originaux n'utilisent que des chemins inexistants.
- *Oracle :* un fichier réel à l'intérieur du dossier est accepté et renvoyé normalisé ; un fichier absent est aussi accepté, sans appel à `toRealPath()` (qui lèverait `NoSuchFileException`). Le mutant de la ligne 308 refuse un fichier légitime ; celui de la ligne 305 appelle `toRealPath()` sur un fichier absent.

**`testResolveWithinRejectsSymlinkEscape`** (0 mutant localement) — *ajouté en seconde séance*
- *Intention :* un lien symbolique placé dans le dossier, mais qui pointe en dehors, doit être refusé (attaque par lien symbolique).
- *Données :* `dossier/lien` → un autre dossier temporaire.
- *Oracle :* la Javadoc et le code : `IOException`, car le chemin réel n'est pas dans le dossier.
- *Remarque :* sous Windows, créer un lien symbolique exige un privilège administrateur ; le test est alors **ignoré** (`Assumptions.abort`). Il s'exécute dans la GitHub Action (Linux), où il tue le dernier mutant de `resolveWithin` (ligne 305, seconde condition), qui n'est pas tuable sans lien symbolique.

**`testPathExactlyMaxLength`** (2 mutants)
- *Intention :* même limite pour `getSanitizedEmbeddedFilePath`, sur la longueur du chemin complet puis sur celle de la partie nom.
- *Données :* `dir/file.txt` (12 caractères, avec `maxLength = 12`) et `dir/abcdefgh.txt` (partie nom de 8 caractères, avec `maxLength = 8`).
- *Oracle :* chemin de longueur égale conservé (`dir/file.txt`) ; chemin trop long, mais nom de longueur égale, donc on renvoie le nom seul non tronqué (`abcdefgh.txt`).

**`testNameExactlyMaxLength`** (1 mutant)
- *Intention :* limite de troncature `namePart.length() > maxLength`.
- *Données :* nom `abcde.txt` avec `maxLength = 5`. La partie nom fait **exactement** la longueur maximale.
- *Oracle :* la condition est stricte (`>`), donc un nom de longueur égale n'est pas tronqué, et on attend `abcde.txt`. Le mutant `>=` tente de tronquer et plante (`substring` négatif).

**`testSuffixLengthBoundary`** (1 mutant)
- *Intention :* limite de longueur des extensions dans `getSuffixFromPath` (`n.length() - i < 6`).
- *Données :* `file.abcd` (point + 4 caractères = 5) et `file.abcde` (point + 5 caractères = 6), de part et d'autre de la limite.
- *Oracle :* la Javadoc indique qu'une extension doit faire au plus 5 caractères, point inclus. Donc `".abcd"`, puis `""`.

**`testDegenerateNamesReturnNull`** (1 mutant)
- *Intention :* les noms inutilisables doivent donner `null`.
- *Données :* `.pdf` (une extension seule), `"\u0000"` (le caractère nul, remplacé par un espace, ce qui donne un chemin vide) et `a/.` (dernier segment `.`).
- *Oracle :* la Javadoc et le code renvoient `null` quand aucun nom de fichier sûr ne peut être construit. Il tue le mutant de la ligne 232 (chemin vide).

**`testCalculateExtension`** (0 mutant attribué)
- *Intention :* tester `calculateExtension`, qu'aucun test original n'appelle directement.
- *Données :* `image/png` (type connu), aucun type (aucun `Content-Type`), et `application/x-ift3913-inconnu` (type inventé).
- *Oracle :* l'extension du registre MIME de Tika (`.png`) ; la valeur par défaut si le type est absent ; `.bin` si le type n'a pas d'extension connue.
- *Remarque :* écrit quand le test généré de `calculateExtension` avait été abandonné (passe 1). Depuis la passe 2, le test généré corrigé tue les mêmes 2 mutants.

### Mutants restants (11) et justification
- **`EndianUtils` L362 et L388** (`int b3 = data[i++] & 0xFF;`) : l'incrément du **dernier** octet lu n'est plus utilisé ensuite. Ce sont des mutants **équivalents**, impossibles à tuer.
- **`FilenameUtils` L156, L215, L320** (`prefixLength > 0` → `>= 0`) : avec `prefixLength = 0`, `substring(0)` renvoie la même chaîne, et `commons-io` renvoie -1 ou une valeur positive dans les autres cas. Mutants **équivalents**.
- **`getPrefixLength` L323 (2 mutants `>=`/`<=` → `>`/`<`) et L324** : ils ne changent le résultat que pour `A:` ou `Z:`, mais `commons-io` renvoie déjà 2 pour toute chaîne `X:` à la ligne 319 : ces cas n'atteignent jamais la ligne 323. Le `return 2` de la ligne 324 est donc du **code mort**, et les deux mutants de limite sont équivalents.
- **L185 et L259** (`return null` après nettoyage du nom) : la partie nom n'est pas blanche à ce moment-là (vérifié juste avant), et les remplacements suivants produisent toujours `_` ou `.`. `trim()` ne retire que les caractères ≤ espace, et `normalize` a déjà remplacé tous les caractères de contrôle. Ces lignes sont **inatteignables**.
- **`resolveWithin` L305, seconde condition** : n'est tuable qu'avec un lien symbolique (le test existe, mais il est ignoré sous Windows ; il s'exécute dans la CI Linux).

---

## 8. Exécution dans la GitHub Action

Le workflow [`.github/workflows/tache2.yml`](.github/workflows/tache2.yml) s'exécute à chaque `push` et `pull_request` (et à la main avec `workflow_dispatch`) :
1. installation de Java 17 (Temurin) ;
2. compilation de `tika-core` et de ses modules parents (`mvn -pl tika-core -am install -DskipTests`) ;
3. exécution de **tous les tests de `tika-core`** (originaux, générés et manuels) ;
4. exécution **séparée des seuls tests ajoutés** (`-Dtest=*_Test,*ManualTest`), pour montrer explicitement qu'ils passent ;
5. analyse de mutation PIT sur les deux classes ;
6. **résumé** dans la page de l'exécution : nombre de tests générés et manuels, mutants tués au total et par classe ;
7. publication des rapports PIT et JaCoCo comme artefacts.

La génération ChatUniTest n'est **pas** relancée dans la CI, car elle exige un modèle local (Ollama). Les tests générés sont commités dans `src/test/java`.

Les workflows d'origine d'Apache Tika (builds complets multi-JDK, Docker) sont désactivés dans l'onglet *Actions* du fork, car ils ne concernent pas ce travail.

**Lien vers une exécution réussie :** *(à compléter après le push)*

Résultats attendus, d'après nos mesures locales avec la même version de Java : 895 tests, 0 échec ; 128 tests générés et 18 tests manuels ; PIT 310/321 ou plus. Sous Linux, `testResolveWithinRejectsSymlinkEscape` s'exécute et devrait tuer un mutant de plus ; certains mutants liés aux chemins Windows peuvent aussi se comporter différemment.

---

## 9. Reproduire

```bash
# compiler
mvn -pl tika-core -am install -DskipTests
# tous les tests + couverture JaCoCo (tika-core/target/site/jacoco)
mvn -pl tika-core test
# seulement les tests ajoutés (générés + manuels)
mvn -pl tika-core test "-Dtest=*_Test,*ManualTest" -Dsurefire.failIfNoSpecifiedTests=false
# mutation (tika-core/target/pit-reports)
mvn -pl tika-core -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage
# mutation avec les seuls tests originaux (étape 1)
mvn -pl tika-core -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage "-DexcludedTestClasses=org.apache.tika.io.*_Test,org.apache.tika.io.*ManualTest"
# les 4 étapes PIT + JaCoCo d'un coup (bash)
bash tache2-rapports/mesures-java17/mesures.sh
# génération (Ollama doit tourner, avec l'alias codeqwen:v1.5-chat)
mvn -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method -DselectMethod=EndianUtils#readIntLE
```

Sous Windows (PowerShell), mettre les arguments `-D...` entre guillemets, et ajouter `-Dcheckstyle.skip -Drat.skip` pour accélérer.

## 10. Fichiers modifiés ou ajoutés

| Fichier | Changement |
|---|---|
| `tika-core/pom.xml` | dépendances de test Mockito ; profil `tache2` (ChatUniTest + PIT) |
| `tika-core/src/test/java/org/apache/tika/io/*_Test.java` | 36 fichiers de tests générés (corrigés), 128 tests |
| `tika-core/src/test/java/org/apache/tika/io/*ManualTest.java` | 2 fichiers de tests écrits à la main, 18 tests |
| `tika-core/chatunitest-tests/` | sortie brute de ChatUniTest (fichiers « réussis » selon l'outil) |
| `.github/workflows/tache2.yml` | GitHub Action |
| `tache2-rapports/mesures-java17/` | mesures finales : PIT et JaCoCo des 4 étapes, journaux, script `mesures.sh` |
| `tache2-rapports/chatunitest-bruts*/`, `chatunitest-erreurs*/`, `logs*/` | tests bruts, tentatives rejetées, journaux de génération (passes 1 et 2) |
| `tache2-rapports/generer.ps1`, `generer-suite.ps1` | scripts de génération des passes 1 et 2 |
| `tache2-rapports/jacoco-*`, `pit-*` | mesures de la passe 1 (Java 25), gardées pour l'historique |
| `README.md` / `README-TIKA.md` | ce rapport / README d'origine de Tika |

Aucun fichier de code source de Tika (`src/main`) ni aucun test original n'a été modifié.

---

## 11. Déclaration d'utilisation de l'IA générative

Conformément aux [directives de l'Université de Montréal](https://boite-outils.bib.umontreal.ca/c.php?g=743753&p=5377614) :

- **ChatUniTest + qwen2.5-coder:7b (Ollama, local)** : génération automatique des tests `*_Test.java`. C'est l'objet même du travail demandé.
- **Claude (Anthropic, via Claude Code)** : aide à la configuration Maven (profil `tache2`, ChatUniTest, PIT), à l'exécution des générations et des analyses, à l'analyse des rapports PIT et JaCoCo, aux corrections des tests générés, à l'écriture des tests manuels et à la rédaction de ce README. Utilisé par les deux membres du binôme, lors de deux séances de travail (première passe de génération, puis seconde passe sur les méthodes restantes).

Toutes les valeurs attendues des tests (générés corrigés et manuels) ont été vérifiées par rapport au code source et à la Javadoc, et tous les chiffres ont été mesurés en exécutant réellement Maven, JaCoCo et PIT.
