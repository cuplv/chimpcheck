# ChimpCheck
Combinator Library for writing test generators and test properties for Android Apps

By Edmund Lam, Peilun Zhang and Bor-Yuh Evan Chang

edmund.lam@colorado.edu, peilun.zhang@colorado.edu and evan.chang@colorado.edu

## Quick Overview

The ChimpCheck repo comprises of three main components:

 * ChimpCombinator: The main scala library that implements the combinator library for generating Chimp tests and operations to run Chimp tests on Android emulators.

 * ChimpDriver: AndroidJUnit harness that integrates with ChimpCombinator. This is the code that is run by the Android emulator and executes event sequences as dictated by the Chimp test.

 * examples: A repository of sample apps to illustrate how ChimpCheck is on Android apps.

## Build Instructions

Current state of ChimpCheck is still experimental, hence build scripts we will write currently assume that you have certain libraries published in your local ivy2 repository. Follow these instructions to get started with ChimpCheck.

First step: Clone and locally publish ScalaBashing at https://github.com/cuplv/ScalaBashing

```
$ git clone https://github.com/cuplv/ScalaBashing.git
<ScalaBashing Root>$ sbt publishLocal
```

This is required by ChimpCombinator. Next, you will need to do the same for ChimpCombinator. Assuming that you have cloned this repository, just do the following:

```
<ChimpCheck Root>/ChimpCombinator$ sbt publishLocal
```

Now, you'll need to do the same again for ChimpDriver. However, ChimpDriver is a gradle project, run the following instead:

```
<ChimpCheck Root>/ChimpDriver$ gradlew assembleDebug
<ChimpCheck Root>/ChimpDriver$ gradlew uploadArchives
```

That's more or less it. The test app(s) will fetch ChimpCombinator and ChimpDriver from your local repositories.

## Running a Sample App

Go to ```<ChimpCheck Root>/examples/ContractionTimerDistilled``` , you will find two folders:

  * ContractionTimerDistilled-App : The Android App with ChimpDriver test harnesses (see TestEspresso)

  * ContractionTimerDistilled-ChimpChecker : Currently just invokes the ChimpDriver JUnit interface with a sample trace. In general, this is where you define generators and property-based tests.

First, compile the App and Android Test harness:

```
<ChimpCheck Root>/examples/ContractionTimerDistilled/ContractionTimerDistilled-App$ ./gradlew assembleDebug
<ChimpCheck Root>/examples/ContractionTimerDistilled/ContractionTimerDistilled-App$ ./gradlew assembleAndroidTest
```

Now copy the APKs (app-debug.apk and app-debug-androidTest.apk) to '/data/chimp/ContractionTimer'

For the next step, you need to start an emulator (preferably x86 and API-23) on port 5554. 

Finally, you can run a quick test script in ```<ChimpCheck Root>/examples/ContractionTimerDistilled/ContractionTimerDistilled-ChimpChecker```:

```
<ChimpCheck Root>/examples/ContractionTimerDistilled/ContractionTimerDistilled-ChimpChecker$ sbt "run edu.colorado.plv.chimp.example.TestApp"
```

If you want to set things up differently, it should be straight-forward to modify 'quickLoad' in TestApp.scala .

## How to start up your own tests

Currently, selective cut and paste from ContractionTimerDistilled is the best option. But here are the main components you need:

  * A ChimpDriver Test Harness: Equivalent of 'TestEspresso' class in the App project. This is the AndroidJUnit4 harness that extends the ChimpDriver.

  * A ChimpChecker Test Generator: Equivalent of TestApp.scala in the ChimpChecker project.

  * .Jar of the Android App classes in 'lib' folder of the ChimpChecker test generator. You will have to manually extract this from your Android App project for now. Will work on a better (gradle) solution to this in the near future.

The third bullet for now, is needed only because we want to reference the R.id.XXX static identifiers of your App. In future, this dependencies
might be more important, as the ChimpDriver Test Harness instance may implement property-based assertions that will be referenced 
(and called via reflection, perhaps??) in the ChimpChecker test generator.

The current way I fulfill this dependency (.Jar in lib folder) is obviously a hack, but we are still looking for a better solution. Embedding this dependency
naively with gradle is not feasible, because 'pure' Java/Scala projects cannot depend on 'Android' projects for some reason (something to do with the Android aar's). 

 

