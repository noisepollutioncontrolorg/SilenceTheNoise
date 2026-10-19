# SILENCEtheNOISE.in — GitHub-ൽ signed APK പ്രസിദ്ധീകരിക്കുന്ന വിധം

ഈ പ്രോജക്റ്റിൽ `.github/workflows/main.yml` ഇതിനകം debug APK build ചെയ്യുന്നു. `.github/workflows/release-signed.yml` ആണ് signing key ഉപയോഗിച്ച് release APK build ചെയ്ത് GitHub Releases-ൽ പ്രസിദ്ധീകരിക്കാൻ ചേർത്ത workflow.

## 1. പ്രോജക്റ്റ് GitHub-ലേക്ക് നൽകുക

1. GitHub-ൽ ഒരു repository സൃഷ്ടിക്കുക. പൊതുവായി എല്ലാവർക്കും APK ഡൗൺലോഡ് ചെയ്യണമെങ്കിൽ repository **Public** ആക്കുക.
2. ഈ ZIP-യിലെ ഉള്ളടക്കങ്ങൾ repository-യുടെ root-ലേക്ക് upload/commit ചെയ്യുക. `app`, `.github`, `build.gradle`, `settings.gradle` എന്നിവ root-ലുണ്ടെന്ന് ഉറപ്പാക്കുക.
3. GitHub-ൽ `Actions` tab തുറന്ന് workflows അനുവദിച്ചിട്ടുണ്ടെന്ന് ഉറപ്പാക്കുക.

## 2. ഒരിക്കൽ മാത്രം signing key സൃഷ്ടിക്കുക (Windows)

കമ്പ്യൂട്ടറിൽ JDK/Java keytool ലഭ്യമാണെന്ന് ഉറപ്പാക്കി PowerShell തുറക്കുക. Signing key സൂക്ഷിക്കാനുള്ള സുരക്ഷിതമായ ഒരു ഫോൾഡറിൽ താഴെയുള്ള command പ്രവർത്തിപ്പിക്കുക:

```powershell
keytool -genkeypair -v -keystore stn-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias stnrelease
```

Password-കളും alias-ഉം സുരക്ഷിതമായി രേഖപ്പെടുത്തുക. Keytool ചോദിക്കുന്ന വിവരങ്ങൾ നൽകുക. `stn-release-key.jks`-ийг GitHub repository-க்கு upload செய்யக்கூடாது; email/WhatsApp വഴി அனுப்பவும் கூடாது.

## 3. Keystore-ஐ Base64-ஆக மாற்றുക

அதே PowerShell window-ல் keystore இருக்கும் folder-க்குச் சென்று இதை இயக்கவும்:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\stn-release-key.jks")) | Set-Content -NoNewline stn-keystore-base64.txt
```

`stn-keystore-base64.txt` திறந்து அதிலுள்ள முழு நீளமான Base64 உரையை copy செய்யவும். இந்த உரையையும் repository-க்கு commit செய்ய வேண்டாம்.

## 4. GitHub Secrets அமைக்கவும்

Repository-ல்:

**Settings → Secrets and variables → Actions → New repository secret**

இந்த நான்கு secrets-ஐ ஒன்றன்பின் ஒன்றாக உருவாக்கவும்:

| Secret name | Value |
|---|---|
| `STN_KEYSTORE_BASE64` | `stn-keystore-base64.txt`-இல் உள்ள முழு உரை |
| `STN_KEYSTORE_PASSWORD` | Keystore உருவாக்கும்போது கொடுத்த keystore password |
| `STN_KEY_ALIAS` | `stnrelease` (அல்லது நீங்கள் தேர்ந்தெடுத்த alias) |
| `STN_KEY_PASSWORD` | Key-க்கு கொடுத்த password |

பின்னர் தற்காலிக `stn-keystore-base64.txt` கோப்பை பாதுகாப்பாக நீக்கலாம். `stn-release-key.jks` கோப்பை மட்டும் பாதுகாப்பான offline backup-களில் வைத்திருக்கவும். GitHub-ல் base64 secret இருப்பதால், அசல் key backup தேவையில்லை என்று நினைக்காதீர்கள்.

## 5. Signed APK உருவாக்கி Release வெளியிடவும்

1. GitHub repository-ல் **Actions** tab திறக்கவும்.
2. **Build and publish signed APK** workflow தேர்ந்தெடுக்கவும்.
3. **Run workflow** அழுத்தி, `Release tag` இடத்தில் `v5.0` உள்ளிடவும். தற்போதைய Gradle project-ல் `versionName` `5.0`, `versionCode` `5` ஆக உள்ளது; அடுத்தடுத்த பதிப்புகளுக்காக இவற்றை `app/build.gradle`-ல் உயர்த்த வேண்டும்.
4. **Run workflow** அழுத்தவும். Build வெற்றிகரமாக முடிந்தால் **Releases** பகுதியில் `SilenceTheNoise.apk` மற்றும் `SilenceTheNoise.apk.sha256` கிடைக்கும்.

அல்லது Git-ல் `v5.0` போன்ற tag push செய்தாலும் workflow இயங்கும். ஒவ்வொரு release-க்கும் புதிய version code அவசியம்; பழைய release APK-களை மறுபடியும் அதே version code-ல் வெளியிட வேண்டாம்.

## 6. எதிர்கால APK download link

Repository public என்றால், உங்கள் GitHub username மற்றும் repository பெயரை மாற்றி இந்த வடிவிலான link-ஐ இணையதளத்தில் வைக்கலாம்:

`https://github.com/OWNER/REPO/releases/latest/download/SilenceTheNoise.apk`

GitHub Release-இல் உள்ள SHA-256 checksum-ஐயும் download செய்பவர்கள் சரிபார்க்கலாம்.

## முக்கிய பாதுகாப்பு குறிப்புகள்

- Signing key, keystore password, key password அல்லது Base64 உரையை source code, issue, log, README அல்லது public repository-ல் இட வேண்டாம்.
- ஒரே `applicationId`-க்கு வரும் அடுத்த release-கள் **அதே signing key**-யால் sign செய்யப்பட வேண்டும். Key தொலைந்தால் அல்லது மாறினால், ஏற்கெனவே install செய்யப்பட்ட release-ஐ update செய்ய முடியாமல் போகலாம்.
- தற்போதுள்ள `app-debug.apk` debug key-யால் sign செய்யப்பட்டிருந்தால், புதிய release key-யால் sign செய்யப்பட்ட APK-ஐ அதன் மேல் நேரடியாக install செய்ய முடியாமல் போகலாம். அந்நிலையில் பழைய app-ஐ uninstall செய்து release APK-ஐ install செய்ய வேண்டியிருக்கும்; uninstall செய்வதற்கு முன் தேவைப்படும் local data-ஐ backup செய்யுங்கள்.
- GitHub Releases வழியாக APK கொடுத்தால், பயனர்கள் Android-இல் browser/file manager-க்கு **Install unknown apps** அனுமதி வழங்க வேண்டியிருக்கலாம். Google Play-இன் automatic update வசதி இதில் இருக்காது.
- இது workflow மற்றும் signing அமைப்பைத் தயாரிக்கிறது; உண்மையான build வெற்றி GitHub Actions ஓடிய பிறகே உறுதி செய்யப்படும்.
