# IFT3913 – Tâche 3 : automatisation des tests de sécurité avec Semgrep

**Binôme :** Messaad Abdelmouhcine (`abdel2154`) · Cherir Roustom Abdeldjalel (`R0us24`)
**Cas d'étude :** Apache Tika (fork de [umontreal-diro/tika](https://github.com/umontreal-diro/tika)), branche `tache3`

> Le rapport de la tâche 2 est sur la branche [`main`](https://github.com/abdel2154/tika/tree/main). Le README original de Tika est dans [README-TIKA.md](README-TIKA.md).

## Résumé

- Le workflow [`semgrep.yml`](.github/workflows/semgrep.yml) **échoue dès qu'un commit introduit une nouvelle vulnérabilité** détectée par Semgrep, et reste vert sinon.
- **7 vulnérabilités de types différents** ont été ajoutées, une par commit. Chaque run est rouge et ne signale **que** la vulnérabilité ajoutée.
- Rapport Semgrep complet : **3 alertes avant** (déjà dans Tika), **11 après** (3 + 8 nouvelles ; la vulnérabilité 4 en déclenche 2).
- Une action réutilisable [`rickroll`](.github/actions/rickroll/action.yml) est appelée quand un test de [`tache2.yml`](.github/workflows/tache2.yml) échoue.

---

## 1. Workflow Semgrep

Fichier : [`.github/workflows/semgrep.yml`](.github/workflows/semgrep.yml), déclenché à chaque `push` et `pull_request`.

1. Checkout avec tout l'historique (`fetch-depth: 0`), installation de Python 3.12 et de `semgrep==1.185.0`.
2. Choix du commit de référence `BASE` : `base.sha` pour une PR, `github.event.before` pour un push, `HEAD~1` pour le premier push d'une nouvelle branche (`before` vaut alors `000…0`).
3. Lancement :
   ```bash
   semgrep scan --config p/java --config p/security-audit --metrics=off \
     --baseline-commit $BASE --error --json-output=semgrep.json <les 5 modules du cas d'étude>
   ```
4. Le rapport `semgrep.json` est sauvegardé comme artefact, même si le job échoue (`if: always()`).

### Choix

| Choix | Pourquoi |
|---|---|
| `--baseline-commit $BASE` | Tika contient **déjà 3 alertes** avant nos changements (section 3). Sans baseline, le build serait toujours rouge. Avec, Semgrep ne signale que les alertes **nouvelles** par rapport à `BASE`, et ne scanne que les fichiers modifiés. |
| `--error` | Le job se termine avec le code 1 s'il reste au moins une alerte : c'est ce qui fait **échouer le build**. |
| `p/java` + `p/security-audit` | Règles Java générales et règles de sécurité (injections, cryptographie, XXE, TLS…). |
| 5 modules seulement | Ce sont les modules du cas d'étude ; scanner tout Tika serait plus long et hors sujet. |
| Version fixée `1.185.0` | Même version qu'en local : les résultats de la CI et nos rapports sont comparables. |
| `--metrics=off` | Aucune donnée d'utilisation envoyée à Semgrep. |
| Artefact JSON | Permet de consulter le détail des alertes de chaque run. |

Semgrep ignore les dossiers de tests par défaut, donc toutes les vulnérabilités sont dans `src/main/java`.

### Validation

| Commit | Attendu | Résultat |
|---|---|---|
| Ajout du workflow seul | vert | [vert](https://github.com/abdel2154/tika/actions/runs/43621239539), aucun fichier Java modifié ([capture](tache3/captures/01-workflow-vert.jpg)) |
| Vulnérabilités 1 à 7 (1 commit chacune) | rouge, 1 seule nouvelle alerte | rouge à chaque fois (liens dans la section 2) |
| Commentaire ajouté dans `MimeTypesReader.java`, qui contient **déjà** une XXE | vert | [vert](https://github.com/abdel2154/tika/actions/runs/43622459870) : le fichier est scanné (« Current version has 1 finding »), mais l'alerte n'est pas nouvelle ([capture](tache3/captures/09-contre-preuve-vert.jpg)) |

Le run de la vulnérabilité 7 confirme aussi la baseline : `NetworkParser.java` contenait déjà l'alerte « socket non chiffré ». Le log affiche « Current version has 2 findings », mais **une seule** est signalée ([capture](tache3/captures/08-vuln7-hostname.jpg)).

Vue d'ensemble des runs : [capture](tache3/captures/vue_globale.jpg).

---

## 2. Les 7 vulnérabilités

Chaque vulnérabilité est une méthode ajoutée à la fin d'une classe existante. Nous avons choisi des classes où la méthode a du sens par rapport à leur rôle, pour que le code paraisse plausible.

| # | Type (CWE) | Fichier | Règle Semgrep | Run |
|---|---|---|---|---|
| 1 | Injection SQL (CWE-89) | `JackcessParser.java` | `formatted-sql-string` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622141763) |
| 2 | Désérialisation non sûre (CWE-502) | `JsonMetadata.java` | `object-deserialization` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622181712) |
| 3 | Hachage faible MD5 (CWE-328) | `PDFParser.java` | `use-of-md5` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622199462) |
| 4 | Chiffrement faible DES/ECB (CWE-326, CWE-327) | `OfficeParser.java` | `des-is-deprecated` + `ecb-cipher` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622219995) |
| 5 | XXE (CWE-611) | `TesseractOCRParser.java` | `documentbuilderfactory-disallow-doctype-decl-missing` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622239832) |
| 6 | Injection de code (CWE-94) | `ConfigValidator.java` | `script-engine-injection` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622258901) |
| 7 | Nom d'hôte TLS non vérifié (CWE-295) | `NetworkParser.java` | `insecure-hostname-verifier` | [rouge](https://github.com/abdel2154/tika/actions/runs/43622278212) |

### 1. Injection SQL – `JackcessParser.findRowsByTableName`
- **Fichier :** `tika-parser-microsoft-module`. Ce parseur lit les bases Microsoft Access, donc chercher une table par son nom y est naturel.
- **Code :** `"SELECT * FROM tables WHERE name = '" + tableName + "'"` exécuté avec `Statement.executeQuery`.
- **Pourquoi c'est vulnérable :** `tableName` vient du fichier analysé. Un nom comme `x' OR '1'='1` modifie la requête. Il faudrait un `PreparedStatement`.
- **Semgrep :** [capture](tache3/captures/02-vuln1-sql.png.jpg).

### 2. Désérialisation non sûre – `JsonMetadata.fromSerializedStream`
- **Fichier :** `tika-serialization`, le module qui sérialise et relit les métadonnées.
- **Code :** `new ObjectInputStream(input).readObject()` sur un flux reçu en paramètre.
- **Pourquoi c'est vulnérable :** `readObject` reconstruit n'importe quelle classe présente dans le classpath avant le cast en `Metadata`. Un flux forgé (chaîne de « gadgets ») peut exécuter du code.
- **Semgrep :** [capture](tache3/captures/03-vuln2-deserialisation.jpg).

### 3. Hachage faible – `PDFParser.computeChecksum`
- **Fichier :** `tika-parser-pdf-module`. L'empreinte sert à identifier un PDF.
- **Code :** `MessageDigest.getInstance("MD5")`.
- **Pourquoi c'est vulnérable :** on sait fabriquer deux fichiers différents qui ont le même MD5 (collisions). Un PDF malveillant peut donc se faire passer pour un autre. Il faudrait SHA-256.
- **Semgrep :** [capture](tache3/captures/04-vuln3-md5.jpg).

### 4. Chiffrement faible – `OfficeParser.decryptLegacyDocument`
- **Fichier :** `tika-parser-microsoft-module`. `OfficeParser` gère déjà les documents Office chiffrés.
- **Code :** `Cipher.getInstance("DES/ECB/PKCS5Padding")`.
- **Pourquoi c'est vulnérable :** DES a une clé de 56 bits, cassable par force brute. Le mode ECB chiffre deux blocs identiques de la même façon, ce qui laisse voir la structure du document. Il faudrait AES en mode GCM.
- **Semgrep :** 2 alertes sur la même ligne, une pour DES et une pour ECB ([capture](tache3/captures/05-vuln4-des.jpg)).

### 5. XXE – `TesseractOCRParser.parseHocrFile`
- **Fichier :** `tika-parser-ocr-module`. Tesseract produit des fichiers hOCR, qui sont du XML.
- **Code :** `DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)`, sans aucune option de sécurité.
- **Pourquoi c'est vulnérable :** par défaut, le parseur accepte un `<!DOCTYPE>` avec des entités externes. Un fichier comme `<!ENTITY x SYSTEM "file:///etc/passwd">` fait lire un fichier local ou contacter un serveur. Tika a pourtant `XMLReaderUtils` pour créer des parseurs sûrs ; la méthode l'ignore.
- **Semgrep :** [capture](tache3/captures/06-vuln5-xxe.jpg).

### 6. Injection de code – `ConfigValidator.evaluateParamExpression`
- **Fichier :** `tika-core`. Cette classe valide les paramètres de configuration ; calculer un paramètre écrit comme `1024 * 1024` y semble utile.
- **Code :** `new ScriptEngineManager().getEngineByName("javascript").eval(expression)`.
- **Pourquoi c'est vulnérable :** la chaîne vient du fichier de configuration et est exécutée comme du code. On peut y écrire `java.lang.Runtime.getRuntime().exec(...)`. (Java 17 n'inclut plus de moteur JavaScript, mais il suffit d'en ajouter un au classpath.)
- **Semgrep :** [capture](tache3/captures/07-vuln6-scriptengine.jpg).

### 7. Nom d'hôte TLS non vérifié – `NetworkParser.acceptAllHostnames`
- **Fichier :** `tika-core`. `NetworkParser` envoie déjà les documents à un serveur distant.
- **Code :** `HttpsURLConnection.setDefaultHostnameVerifier(...)` avec un `HostnameVerifier` dont `verify` retourne toujours `true`.
- **Pourquoi c'est vulnérable :** n'importe quel certificat valide est accepté, même s'il est émis pour un autre domaine. Un attaquant entre le client et le serveur (*man-in-the-middle*) peut lire et modifier les échanges. De plus, le réglage s'applique à **toutes** les connexions HTTPS de la JVM.
- **Semgrep :** [capture](tache3/captures/08-vuln7-hostname.jpg).

**Essais abandonnés :** l'injection de commande (`Runtime.exec`) et `new Random()` n'ont pas été détectés par ces règles. Nous les avons remplacés.

---

## 3. Comparaison des rapports avant / après

Scan complet des 5 modules, sans baseline, avec Semgrep 1.185.0 : [`semgrep-avant.json`](tache3/semgrep-avant.json) (avant nos commits) et [`semgrep-apres.json`](tache3/semgrep-apres.json) (après la vulnérabilité 7).

| Fichier | Règle | Avant | Après |
|---|---|---|---|
| `MimeTypesReader.java` | `saxparserfactory-disallow-doctype-decl-missing` | ligne 223 | ligne 224 |
| `NetworkParser.java` | `unencrypted-socket` | ligne 68 | ligne 71 |
| `XMLReaderUtils.java` | `xmlinputfactory-possible-xxe` | ligne 307 | ligne 307 |
| `JackcessParser.java` | `formatted-sql-string` | — | **nouvelle** (154) |
| `JsonMetadata.java` | `object-deserialization` | — | **nouvelle** (135) |
| `PDFParser.java` | `use-of-md5` | — | **nouvelle** (795) |
| `OfficeParser.java` | `des-is-deprecated` | — | **nouvelle** (398) |
| `OfficeParser.java` | `ecb-cipher` | — | **nouvelle** (398) |
| `TesseractOCRParser.java` | `documentbuilderfactory-disallow-doctype-decl-missing` | — | **nouvelle** (840) |
| `ConfigValidator.java` | `script-engine-injection` | — | **nouvelle** (69) |
| `NetworkParser.java` | `insecure-hostname-verifier` | — | **nouvelle** (177) |
| **Total** | | **3** | **11** |

Les 3 alertes d'origine sont toujours là ; seuls leurs numéros de ligne ont bougé, à cause des lignes ajoutées au-dessus. Les 8 nouvelles alertes correspondent exactement aux 7 vulnérabilités.

---

## 4. Rickroll

- **Action :** [`.github/actions/rickroll/action.yml`](.github/actions/rickroll/action.yml), une action composite réutilisable. Nous avons écrit la nôtre parce que [random-rickroll](https://github.com/TejasvOnly/random-rickroll) tourne sous `node12`, qui n'est plus supporté par GitHub, et qu'elle ne fait que commenter des issues. Nous avons repris son GIF.
- **Ce qu'elle fait :** une annotation d'erreur avec le lien vers la vidéo, et le GIF de Rick Astley dans le résumé du run.
- **Appel :** dans [`tache2.yml`](.github/workflows/tache2.yml), juste après les deux étapes de tests :
  ```yaml
  - name: Rickroll si un test echoue
    if: failure() && (steps.tests.outcome == 'failure' || steps.tests_ajoutes.outcome == 'failure')
    uses: ./.github/actions/rickroll
  ```
  La condition limite le rickroll à l'échec d'un **test**, pas à une erreur de compilation.

### Validation

| Commit | Résultat |
|---|---|
| Ajout de l'action | [vert](https://github.com/abdel2154/tika/actions/runs/43650529532), pas de rickroll |
| Test cassé exprès (`EndianUtilsManualTest`, valeur attendue changée) | [rouge + rickroll](https://github.com/abdel2154/tika/actions/runs/43650545694) : l'étape de tests échoue, l'étape rickroll s'exécute |
| Test réparé | [vert](https://github.com/abdel2154/tika/actions/runs/43650569419), pas de rickroll |

---

## 5. Déclaration d'utilisation de l'IA générative

Conformément aux [directives de l'Université de Montréal](https://boite-outils.bib.umontreal.ca/c.php?g=743753&p=5377614) :

- **Claude (Anthropic, via Claude Code)**, utilisé par les deux membres du binôme : aide à l'écriture du workflow Semgrep et de l'action rickroll, au choix des vulnérabilités et à la vérification qu'elles sont détectées, à l'exécution des scans et à la rédaction de ce README.

Tous les résultats ont été vérifiés en exécutant réellement Semgrep, en local et dans GitHub Actions (liens ci-dessus).
