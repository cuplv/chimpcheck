So, let's say that you have an application that crashes, and you don't know why it's crashing. Well, to start, we have a simple script right here; this script is called relevantMonkey, which is a series of commands that goes through an application randomly clicking and prodding. We can see that we have commands like Click(*) <+> LongClick(*), which is clicking or long-clicking a value. You can also see that the program can swipe at the screen, pinch the screen at any point, and rotate the screen.

Huh. It looks like we need to login to the system. Fortunately, with ChimpCheck, we're able to do this easily. We just need to write a script where we go to the login screen, type in our information in the boxes (that being the hostUrl, the username, and the password), and then we should be able to log in. We can use the login trace ( *>> ) to simply log into the application before we do random inputs. From there, we can login.

Okay. Now, it seems like doing a ton of interrupts make no sense on the login screen, so instead let's just use the login without interrupts. This is as easy as changing the *>> with a :>>. From there, we run it and see what happens.

Huh. It looks like there is a permissions screen on this app. However, this permission screen may not show up for all Android emulators, or it might show up on a different screen. To be sure that we've got it, we'll have to make sure that we have a conditional in our ChimpCheck statement which we have as the trace permiss; if "Allow" is displayed at any point, let's have ChimpCheck click "Allow". 

Now that that works, we can see that the application just moves around. However, we can decrease the amount of things that we may want to do to the application based on information that we know. We feel as though the crash might have something to do with a mix between Long Clicks, Clicks, Rotation, and ClickMenu, so we should be able to write a script that focuses more heavily on those things. To do this, we introduce Gorilla, and write our own configuration for that Gorilla saying that we want to be able to rotate, click the menu, do a long click, and click equally as likely. A thing to look out for here is that the emulator may or may not need to catch up with the chimpcheck script, so to be sure, we need to change this to a Subservient Gorilla to make sure that after every action, we sleep for a half a second. This makes sure that the emulator can catch up and we don't block chimpcheck.

It looks as though sometimes, we produce some sort of crash; This crash seems to produce some sort of Stack Trace, which is shown in the bottom right corner of the demo. Eventually, after running the demo several times, we have enough information of how we crash the application that we can build a script to crash it very easily.

And from there, we have used Chimpcheck to reproduce a crash.

-- NOTES --

App crashes, don't know how to reproduce this crash.
	How do I crash it? User inputs that must be done. Difficult to get the right sequence
	App that crashes, stack trace.
	How do I reproduce the stack trace?
	Script multiple interactions; create a space of interactions
	
-- OLD SCRIPT --
Hello! This is the demo for ChimpCheck, which is a Domain Specific Language that tests an application. 

When you start the demo, you first start by picking one of three applications: The first application is Kisten, which is a simple application that has a few utilities like a calculator and a timer. The second application is NextCloud, which is an online data storage system that's somewhat similar to Dropbox. The third application is ChimpTrainer, which is an example app that has a slider and a countdown. From there, you have two different tests that you need to pick. In the case where we picked Kisten, the first test case is one where we basically click randomly, and the second test case shows how we can use ChimpCheck to crash the application.

As an example, we're going to use the first test case for Kisten, where we click randomly 500 times. Note that with this test we have this section of the code here (isDisplayed(\"Turm\") Then ClickBack:>>Skip); in this section of the test, we have this code; This is due to the fact that when Kisten reaches the Turm screen, it's just a blank screen with nothing to click on, which would block any attempt to click on a random button; instead, we need to click back whenever we reach this screen.

You should notice that a ChimpCheck script is an expression that ends with an EventTrace, or a series of commands that ChimpCheck uses to test the application. The Click(*) tells us to click randomly, while the Repeat 500 tells us to repeat the command 500 times. In this demo, you're able to show  For the purposes of this video, we'll claim that this is too long, and we'll edit this to 10 times. To do this, we just change the 500 to a 30, and then click Generate.

When we click Generate, the test gets sent to the emulator to then run with the APK. When the test builds, we will be able to see the emulator run our test in real time, such as right now. You'll notice that the emulator clicks 30 times in random positions. If it ever reaches the Turm screen, (as it did while I was talking?), then it hits the back button on the emulator, moving it to a different screen.

When the emulator finishes running the tests, it produces the result of the test; In this case, it is a Success, as the test ran without running into an error. On the right, we have the sequence of traces that was run before the test finished, either by a crash or by a finished test.

Let's run a trace that will crash the application to show how the demo reacts. Kisten is fairly easy to crash as you can see on the emulator to the right; We just need to start a countdown and then move to a different screen. This leads to the output showing the trace before it crashed, and it provides a stack trace so you can see what had occurred to make the emulator actually crash. 
