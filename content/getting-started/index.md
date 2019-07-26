---
date: 2017-12-14T15:07:13+01:00
title: 'ChimpCheck: Concise and Effective Android Testing Library'
weight: 10 
---
## High-level description of the architecture

1. Compile Android Apps with ChimpCheck library
2. Use ChimpCombinator to generate the test script
3. Use ScalaBashing to run the script (suggested but optional)

## Video tutorial

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/pUvbm5g2pWM?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

## Build Instructions

Current state of ChimpCheck is still experimental, hence build scripts we will write currently assume that you have certain libraries published in your local ivy2 repository. Follow these instructions to get started with ChimpCheck.


**1. Building dependencies and libraries**
First, please clone ChimpCheck from its GitHub repo and init its submodule.
```
git clone https://github.com/cuplv/chimpcheck.git
git submodule init
git submodule update
```
**2. Build and upload ChimpDriver in local ivy2 repo**
```
<ChimpCheck Root>/ChimpDriver$ ./gradlew uploadArchives
```
**3. Set up ScalaBashing and ChimpCombinator**

You need to have **sbt** installed. See instructions [here](https://www.scala-sbt.org/1.0/docs/Setup.html)
```
<ChimpCheck Root>/ScalaBashing$ sbt publishLocal
```

This is required by ChimpCombinator. Next, you will need to do the same for ChimpCombinator. Assuming that you have cloned this repository, just do the following:

```
<ChimpCheck Root>/ChimpCombinator$ sbt publishLocal
```

**4. Configurate your Android application**:

  In the build.gradle of the app module, add ChimpCheck dependency and use ChimpCheck as the testInstrumentationRunner:
```
android {
  defaultConfig {
    ...
    testInstrumentationRunner "edu.colorado.plv.chimp.driver.ChimpJUnitRunner" 
  }
}
repositories {
  ...
  mavenLocal()
  ...
}
dependencies {
  ...
  compile (group: 'edu.colorado.plv.fixr',
            name : 'chimpDriver',
            version: '1.0', configuration: "compile")
  ...
    
}
```

**5. Add a tester class in the app**:


In the path `src/androidTest/java/**com.packagename.sample**/`, create a file (e.g TestExpresso):
```
package com.packagename.sample; // replace with your app's package name

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.runner.RunWith;
import edu.colorado.plv.chimp.driver.EspressoChimpDriver;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestExpresso extends EspressoChimpDriver {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

}
```

`MainActivity` is the activity where you want to start your test.


**6. Compile your Android apk**


Run in command line (or click in Android Studio)
```
./gradle assembleDebug
./gradle assembleAndroidTest
```
and find your apks at 
`path-to-project/app/build/outputs/apk/` and we want `app-debug.apk` and `app-debug-androidTest.apk`



## Try this checker sample!
You need to have **adb** in the `$PATH`


We have a skeleton testing template for you already:

```
https://github.com/cuplv/chimpcheck/tree/master/examples/

```

Compile the Android project in `ChimpTrainer` and use `ChimpChecker` to test it!

Please refer ``https://github.com/cuplv/chimpcheck/blob/master/examples/ChimpChecker/src/main/scala/edu/colorado/plv/chimp/example/TestApp.scala`` for how to write ChimpCheck scripts.

Sample Scripts:
```
Sleep(3000):>> Click("Begin") :>> Type(*, "test") :>> Type(*, "test") :>> Click("Login") :>> Click("Countdowntimer Testing") :>> Click("5 seconds") :>> ClickBack

Click(R.id.buttonOK) :>> (isDisplayed("Allow") Then (Click("Allow") :>> Skip)) :>> LongClick("*")
```



## How to start up your own tests

*Currently, selective cut and paste from ChimpChecker is the best option.* 
[ChimpCheck example here](https://github.com/cuplv/chimpcheck/blob/master/examples/ChimpChecker/src/main/scala/edu/colorado/plv/chimp/example/TestApp.scala)

But here are the main components you need:

  * A ChimpDriver Test Harness: Equivalent of 'TestExpresso' class in the App project. This is the AndroidJUnit4 harness that extends the ChimpDriver. This is just boilerplate code and all you need is to instantiate 'ActivityTestRule' with your app's main activity class.

  * A ChimpChecker Test Generator: Equivalent of TestApp.scala in the ChimpChecker project. Its the main loader for your test cases.

  * .Jar of the Android App classes in 'lib' folder of the ChimpChecker test generator. You will have to manually extract this from your Android App project for now. Will work on a better (gradle) solution to this in the near future.

The third bullet for now, is needed only because we want to reference the R.id.XXX static identifiers of your App, but in future that could be more reasons for this dependency..

The current way I fulfill this dependency (.Jar in lib folder) is obviously a hack, but we are still looking for a better solution. Embedding this dependency
naively with gradle is not feasible, because 'pure' Java/Scala projects cannot depend on 'Android' projects for some reason (something to do with the Android aar's). 

