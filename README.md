# ChimpCheck

ChimpCheck is a property-based testing tool for interactive apps.

Rich interactive applications, such as apps on mobile platforms, are complex stateful and often distributed systems where sufficiently exercising the app with user-interaction (UI) event sequences to expose defects is both hard and time-consuming. In particular, there is a fundamental tension between brute-force random UI exercising tools, which are fully-automated but offer low relevance, and UI test scripts, which are manual but offer high relevance. With ChimpCheck, we fuse scripting with randomized UI testing, enabling programming, generating, and executing property-based randomized test cases for Android apps.

By Edmund Lam, Peilun Zhang and Bor-Yuh Evan Chang

edmund.lam@colorado.edu, peilun.zhang@colorado.edu and evan.chang@colorado.edu

## Quick Overview

The ChimpCheck repo comprises of three main components:

 * ChimpCombinator: The main scala library that implements the combinator library for generating Chimp tests and operations to run Chimp tests on Android emulators.

 * ChimpDriver: AndroidJUnit harness that integrates with ChimpCombinator. This is the code that is run by the Android emulator and executes event sequences as dictated by the Chimp test.

 * examples: A repository of sample apps to illustrate how ChimpCheck is on Android apps.

## Build Instructions

please visit the site to see building instructions:

http://plv.colorado.edu/ChimpCheck/
