# GnirehtetX
Basically the stock [Gnirehtet](https://github.com/Genymobile/gnirehtet) app with some extra features, such as

* block internet access for apps
* set custom DNS servers
* stop Gnirehtet on disconnect

## Build in release mode
1. Put your keystore `output.jks` in the `app/` folder.
2. Put the properties `keystore-store-password`, `keystore-key-alias`, and `keystore-key-password` in the gradle.properties file.

Finally run
```
./gradlew assembleRelease
```
and you should be able to find the APK at `app/build/outputs/apk/release/app-release.apk`.
