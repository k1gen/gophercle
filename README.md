# gophercle - minimalist gopher client for android

### gophercle lets you access the gopherspace in a very efficient, resource-friendly way.

this is a fork of https://gitlab.com/biotstoiq/gophercle, all credit goes to him
~~there most probably won't be any new features/improvements because I don't know Java~~
nvm, I've rewritten the entire thing in Kotlin and with help
of [01LiDev](https://github.com/01LiDev) it's fully functional
this repo exists because I wanted to use #000000 black background instead of grey one
(edit: also, to practice Kotlin and maintain something)

### features

- global search url
- bookmarks
- downloads
- set text size
- dark theme
- minimalist and lightweight

### build

build from android studio

- clone this repository (`git clone https://gitlab.com/k1gen/gophercle.git`)
- open the project in Android Studio
- select build variant (Build -> Select Build Variant -> release)
- choose Build Bundle(s) / APK(s) -> Build APK(s)

build from commandline

- first, please go through the build.gradle file to see the dependencies
- make sure you have the commandline tools
  installed (https://developer.android.com/studio#cmdline-tools)
- make sure you have the SDK installed
- clone this project (`git clone https://gitlab.com/k1gen/gophercle.git`) and cd into it
- create/edit the local.properties file and give SDK location in the
  file (`sdk.dir=/absolute/path/to/SDK`)
- execute `./gradlew assembleRelease`
- execute `adb install app/build/outputs/apk/release/*.apk`
- you can also sign you APK and install it with `adb install app/release/app-release.apk`

or you can use the apk I built in app/release, but you really shouldn't trust some guy on the
internet

### license

- MIT license

### contributions

there are a lot of ways in which you can contribute. you can help spread the word; you can
also contribute code/ideas/artwork/etc. any sort of help is appreciated.
