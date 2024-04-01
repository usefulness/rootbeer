# `rootbeer`
[![Build Project](https://github.com/usefulness/rootbeer/actions/workflows/default.yml/badge.svg?branch=master&event=push)](https://github.com/usefulness/rootbeer/actions/workflows/default.yml)
![Maven Central](https://img.shields.io/maven-central/v/io.github.usefulness/rootbeer)


## Usage

```kotlin
val rootBeer = RootBeer(context)
if (rootBeer.isRooted) {
    //we found indication of root
} else {
    //we didn't find indication of root
}
```

You can also call each of the checks individually as the sample app does. It is advisable to call `isRooted()` from a background thread as it involves disk I/O.

### False positives

Manufacturers often leave the busybox binary in production builds and this doesn't always mean that a device is root. We have removed the busybox check we used to include as standard in the isRooted() method to avoid these false positives.

If you want to detect the busybox binary in your app you can use `checkForBinary(BINARY_BUSYBOX)` to detect it alone, or as part of the complete root detection method:

```java
rootBeer.isRootedWithBusyBoxCheck();
```

The following devices are known the have the busybox binary present on the stock rom:
* All OnePlus Devices
* Moto E
* OPPO R9m (ColorOS 3.0,Android 5.1,Android security patch January 5, 2018 )

### Dependency

Available on [maven central](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22rootbeer-lib%22), to include using Gradle just add the following:

```java
dependencies {
    implementation 'io.github.usefulness:rootbeer-core:{{ version }}'
}
```

### Thanks

* Kevin Kowalewski and others from this popular [StackOverflow post](https://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device?rq=1)
* Eric Gruber's - Android Root Detection Techniques [article](https://blog.netspi.com/android-root-detection-techniques/)


## Other libraries

If you dig this, you might like:

 * Tim Strazzere's [Anti emulator checks](https://github.com/strazzere/anti-emulator/) project
 * Scott Alexander-Bown's [SafetyNet Helper library](https://github.com/scottyab/safetynethelper) - coupled with server side validation this is one of the best root detection approaches. See the [Google SafetyNet helper docs](https://developer.android.com/training/safetynet/index.html).
