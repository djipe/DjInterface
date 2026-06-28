# Interface DJ — Application Android

Application Android native (Java) pour gérer les demandes de titres musicaux depuis votre mobile.
Équivalent de `dj.html` mais en application Android compilée, compatible **Android 5.0+ (API 21)**.

---

## Fonctionnalités

- **Liste des demandes** avec pochette album, titre, IP et heure
- **Bouton "Ajouter à la File"** → appelle `dj_api.php?action=update_status` avec statut `Joué` → ajoute à la queue Spotify
- **Bouton "Ignorer"** → statut `Ignoré`
- **Connexion DJ Spotify** → efface l'ancien token puis ouvre l'OAuth dans le navigateur
- **Effacer la liste** → confirmation puis `clear_log`
- **Rafraîchissement** : pull-to-refresh + bouton toolbar
- **Auto-refresh toutes les 30 s** (configurable dans `MainActivity`)

### Page Paramètres (icône ⚙️)

- **Activer/désactiver** les notifications
- **Délai avant alerte** : curseur 3 → 30 minutes (défaut : 3 min)
- **Type de notification** : Vibration | Bip sonore | Vibration + Bip | Visuel uniquement
- **Snooze 12h** : suspend les notifications avec affichage de l'heure de reprise

### Notifications de fond (WorkManager)

Un `PollWorker` tourne en arrière-plan (toutes les 15 min, minimum Android).
Il interroge l'API et déclenche une notification si au moins une demande est en attente depuis plus du délai paramétré.

---

## Compilation

### Prérequis

- [Android Studio Hedgehog ou plus récent](https://developer.android.com/studio) (gratuit)
- JDK 17 inclus dans Android Studio
- Connexion internet (pour télécharger les dépendances Gradle)

### Étapes

1. Ouvrir Android Studio → **File > Open** → sélectionner le dossier `DjInterface/`
2. Attendre la synchronisation Gradle (première fois ~2-3 min)
3. **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. L'APK se trouve dans `app/build/outputs/apk/debug/app-debug.apk`

### Installer sur le téléphone

```bash
# Via ADB (téléphone branché en USB avec débogage USB activé)
adb install app/build/outputs/apk/debug/app-debug.apk
```
Ou copier l'APK sur le téléphone et l'ouvrir (activer "Sources inconnues" dans les paramètres).

---

## Configuration

Toutes les constantes modifiables sont dans `AppPrefs.java` :

```java
public static final String API_BASE_URL         = "https://lapieceuniquedijon.great-site.net/spotify/dj_api.php";
public static final String SPOTIFY_CLIENT_ID    = "bc3aa6e32833442294930bc5acfc1a6a";
public static final String SPOTIFY_REDIRECT_URI = "https://lapieceuniquedijon.great-site.net/spotify/spotify-callback.php";
```

---

## Structure du projet

```
DjInterface/
├── app/
│   ├── build.gradle                          ← dépendances, minSdk 21
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/net/lapieceuniquedijon/djinterface/
│       │   ├── MainActivity.java             ← écran principal
│       │   ├── SettingsActivity.java         ← page paramètres
│       │   ├── RequestAdapter.java           ← liste RecyclerView
│       │   ├── TrackRequest.java             ← modèle de données
│       │   ├── ApiClient.java                ← appels réseau OkHttp
│       │   ├── AppPrefs.java                 ← SharedPreferences centralisé
│       │   ├── NotificationHelper.java       ← canal + envoi notification
│       │   └── PollWorker.java               ← polling arrière-plan WorkManager
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── activity_settings.xml
│           │   └── item_request.xml
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           ├── drawable/
│           │   ├── ic_refresh.xml
│           │   ├── ic_settings.xml
│           │   └── ic_back.xml
│           ├── menu/main_menu.xml
│           └── xml/network_security_config.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```
