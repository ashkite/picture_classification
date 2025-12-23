# Release Guide (Play Console)

## 1) Create a release keystore

```bash
keytool -genkeypair -v \
  -keystore picture_classification.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias picture_classification
```

## 2) Add keystore.properties

Copy `keystore.properties.example` to `keystore.properties` and fill in values:

```properties
storeFile=/absolute/path/to/picture_classification.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=picture_classification
keyPassword=YOUR_KEY_PASSWORD
```

## 3) Build a release bundle

```bash
./gradlew bundleRelease
```

Output:
- `app/build/outputs/bundle/release/app-release.aab`

## 4) Play Console checklist

- Create app listing
- Upload AAB to internal testing
- Add screenshots, icon, feature graphic
- Data Safety: on-device only, no data collected
- Privacy policy: host `docs/privacy_policy.md` and add URL
- Roll out to production when ready
