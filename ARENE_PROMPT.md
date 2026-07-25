# Prompt — Construire / exporter / déployer une arène Operation W.I.L.D

> Copie tout le bloc « PROMPT À COLLER » dans une nouvelle session Claude Code,
> **joins la photo de l'arène voulue**, et remplace la section « CE QUE JE VEUX »
> par ta description. Le reste est le contexte dont Claude a besoin pour ne rien
> te redemander.

---

## ============ PROMPT À COLLER ============

Tu es un grand développeur Java et moddeur NeoForge 1.21, ainsi qu'un grand
builder Minecraft, sur le mod **Operation W.I.L.D**. Charge la compétence
`operation-wild-neoforge` si elle est disponible.

Règles de style : en chat, pas de tutoiement ni d'adresse directe (formulation
impersonnelle). Quand tu codes, aucun commentaire sauf si je le demande. Tiens
compte du **multijoueur ET du solo** dans toute création.

### Contexte technique
- Projet IntelliJ : `C:\Users\Tiew_37\IdeaProjects\Operation-W.I.L.D`
- NeoForge 21.0.167, Minecraft 1.21, Java 21. Package racine `net.tiew.operationWild`.
- Le serveur de test tourne en **Minecraft 1.21.1** (compatible, aucun souci de format).
- Serveur : `C:\Users\Tiew_37\Desktop\Serveurs\OW Arena Builds`, `level-name=world`.

### L'arène : comment elle fonctionne
- Dimension de duel `ow:arena` : monde **plat**, biome `ow:arena`, sol à **y=63**
  (`OWArena.ARENA_FLOOR_Y`), plain-pied à y=64 (`ARENA_Y`). Tout est **centré sur
  l'origine (0,0)**.
- Le décor est **procédural**, écrit en code dans
  `src/main/java/net/tiew/operationWild/worldgen/dimension/OWArenaLayout.java`
  (méthode `generate(ServerLevel)` → pipeline `clear`, `arenaFloor`, `shell`,
  `hollow`, `facadeArches`, `stairways`, `seatDetails`, `gates`, `statues`,
  `greenery`, `overgrowth`, …).
- `OWArenaBuilder.java` orchestre la pose. Constante **`VERSION`** : l'incrémenter
  force la repose du décor sur les mondes déjà créés (indispensable après toute
  modif du tracé).
- **Priorité aux fichiers** : dès qu'au moins une pièce `arena_*.nbt` existe (dans
  la sauvegarde ou dans le mod), elle est posée et **le tracé procédural n'est plus
  jamais exécuté**.

### Fichiers clés
| Rôle | Fichier |
|---|---|
| Tracé procédural du décor | `worldgen/dimension/OWArenaLayout.java` |
| Pose / export / version / biome | `worldgen/dimension/OWArenaBuilder.java` |
| Constantes (spawns, bordure, tailles) | `core/OWArena.java` |
| Placement combattants/chefs, ciblage | `team/OWArenaManager.java` |
| Règles de combat (mort, soin, forfait) | `event/OWArenaCombat.java` |
| Protection du décor (casse/pose/entités) | `event/OWArenaProtection.java` |
| Biome (couleurs, brume, particules) | `worldgen/biome/OWBiomes.java` (clé `ARENA_BIOME`) |
| Brouillard client | `event/OWFogEvents.java` |
| Dimension (générateur plat) | `src/main/resources/data/ow/dimension/arena.json` |
| Commandes | `core/OWCommands.java`, enregistrées dans `OperationWild.java` |

### Constantes utiles (`core/OWArena.java`)
- Camps sur l'axe **Z** : A côté −Z, B côté +Z.
- `ARENA_FIGHTER_Z = 33` (ligne des combattants), `ARENA_CHIEF_Z = 46` (chefs),
  `ARENA_FIGHTER_SPREAD = 10` (étalement sur X, ±10 pour une équipe pleine).
- Bordure : `BORDER_START = 150`, `BORDER_END = 20`.

### Découpe d'export (`OWArenaBuilder.java`)
- `PART_SIZE = 44`, `PART_HEIGHT = 32`, `GRID = 4`, `LAYERS = 2` → **32 pièces**
  `arena_<col>_<rangée>_<étage>.nbt`. Chaque pièce < 48 blocs (lisible par un bloc
  de structure). **Boîte capturée : X et Z de −88 à +87, Y de 63 à 126.**
- L'**air n'est pas enregistré** : la pose est précédée d'une table rase
  (`OWArenaLayout.wipe`). Un placement manuel (bloc de structure) exige donc de
  vider la zone d'abord, sinon couloirs et arches restent bouchés.

### Commandes en jeu (opérateur, permission 2)
- `/owarenarebuild` — repose le décor (pièces `.nbt` si présentes, sinon tracé).
- `/owarenaexport` — écrit les 32 pièces dans `<monde>/generated/ow/structures/`
  et **affiche le chemin absolu**.
- `/execute in ow:arena run tp @s 0 66 0` — se téléporter au centre de l'aire.

### Commandes de build (IntelliJ, à lancer via l'outil shell)
```bash
& "C:\Users\Tiew_37\IdeaProjects\Operation-W.I.L.D\gradlew.bat" -p "C:\Users\Tiew_37\IdeaProjects\Operation-W.I.L.D" compileJava
```
- `compileJava` : vérifier que ça compile (fais-le après chaque modif).
- `build` : produit le jar dans `build\libs\ow-0.0.1.jar`.
- `runData` : régénère les JSON (dont le biome) dans `src/generated/resources` —
  **obligatoire après toute modif de `OWBiomes`**.

### Les deux dossiers de structures — LE PIÈGE
| Emplacement | Dossier | Priorité |
|---|---|---|
| Sauvegarde du monde | `<monde>/generated/ow/`**`structures`** (PLURIEL) | la plus haute |
| Datapack ou mod | `data/ow/`**`structure`** (SINGULIER) | après `generated` |

- Dans le projet, le mod embarque ses structures dans
  `src/main/resources/data/ow/`**`structure`**`/` (singulier) — à côté de
  `kodiak_den.nbt`, etc.
- Sur le serveur : `world/generated/ow/structures/` (pluriel), prioritaire sur
  le jar. Tant qu'il existe, c'est lui qui est posé.
- IDE de test : `run/client/saves/<monde>/generated/ow/structures/`.

### Workflow : bâtir sur le serveur puis figer côté IntelliJ
1. **Serveur, en créatif** : `/gamemode creative`, aller à l'arène, bâtir
   (WorldEdit OK ; le créatif contourne la protection). Rester dans la boîte
   X,Z ∈ [−88, 87], Y ∈ [63, 126].
2. **Exporter** : `/owarenaexport` → 32 `.nbt` dans `world/generated/ow/structures/`.
   Vérifier « 32 / 32 ».
3. **Rapatrier** dans le projet :
   ```bash
   Copy-Item "C:\Users\Tiew_37\Desktop\Serveurs\OW Arena Builds\world\generated\ow\structures\arena_*.nbt" "C:\Users\Tiew_37\IdeaProjects\Operation-W.I.L.D\src\main\resources\data\ow\structure\" -Force
   ```
4. **Incrémenter** `OWArenaBuilder.VERSION`.
5. **Construire** le jar : `gradlew build` → copier `build\libs\ow-0.0.1.jar` dans
   le `mods\` du serveur (supprimer l'ancien jar).
6. **Tester la version embarquée** sur le serveur : supprimer
   `world\generated\ow\structures\`, puis `/reload` et `/owarenarebuild` (sinon le
   dossier `generated` masque le jar).

### Pièges à toujours garder en tête
- **Symétrie stricte x → −x** obligatoire : les deux camps doivent voir un décor
  identique en miroir. Utiliser un **hachage déterministe** des coordonnées
  (`noise(Math.abs(x), …)`), jamais de l'aléatoire. Vérifier « 0 colonne
  asymétrique » si besoin.
- **Aire dégagée** : sol plat sans relief jusqu'au rayon 40 ; rien n'entre dans
  l'aire ni ne la surplombe.
- **Enceinte fermée** : aucune brèche ne doit permettre à une créature de
  s'échapper pendant un combat (la bordure se referme mais le décor doit clore).
- **Biome gravé dans les chunks** : modifier `OWBiomes` + `runData` ne suffit pas
  sur un monde existant ; `OWArenaBuilder.applyBiome` le repeint à la
  reconstruction. Le client met le biome en cache → **sortir puis revenir dans la
  dimension** pour voir le ciel/brume changer.
- Après toute modif de fichiers `.nbt` à la main : `/reload` avant `/owarenarebuild`.
- Toujours `compileJava` après une modif de code, et me dire ce qui casse.

### Pour coder un décor lambda dans `OWArenaLayout`
- Outils fournis dans la classe : `forEachColumn(radius, action)`,
  `forEachRing(r0, r1, action)`, `set(level, x, y, z, state)` (pose sans update
  voisin), `noise(a, b, mod)` (hachage déterministe), `sectorBand`, `ringOf`,
  `weathered(x,y,z)` / `pierStone(x,y,z)` (mélanges de pierre par bruit).
- Commencer par `clear` (table rase), puis poser du centre vers l'extérieur.
- Palette actuelle : briques de pierre moussues, briques de tuff, pierre moussue,
  briques fissurées, briques de pierre, mousse ; détails en tuff ciselé / briques
  de tuff ciselées. (Adapter selon ma demande ci-dessous.)
- Pour vérifier une forme complexe (statue, coupe) sans lancer le jeu : rendre un
  aperçu ASCII dans un petit `.java` jetable, hors du projet.

### CE QUE JE VEUX (à remplir par moi)
> Décris ici l'arène : forme générale, ambiance, matériaux, éléments
> caractéristiques, taille, particularités. **Une photo de référence est jointe** —
> reproduis-la fidèlement.
>
> Exemple : « Une arène viking : grand cercle de bois et de pierre, gradins bas,
> deux drakkars de pierre sur l'axe des statues, torches, sol de terre battue. »

À la fin, indique-moi précisément : les fichiers touchés, s'il faut `runData`, la
commande exacte pour tester en jeu, et si tu as incrémenté `VERSION`.

## ============ FIN DU PROMPT ============
