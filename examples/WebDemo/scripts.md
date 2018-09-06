# Scripts pre-loaded in the web demo

## Kistenstapleln App

### Randomly Click

```scala
// This randomly clicks 500 times; 
// If it ever reaches the Turm screen, it will go back to the previous screen.

val checkTurm = 
  Try((isDisplayed(\"Turm\") Then ClickBack:>>Skip).generator.sample.get)
Repeat(500, Click(*) :>> checkTurm) :>> Skip",
```

### Crash on Countdown

```scala
// This crashes the app by finishing a countdown on another page.

Click(\"Countdown\") :>> Click(\"0:10\") :>> Click(\"Countdown\") :>> 
  Click(\"Punktzahl berechnen\") :>> Sleep(10000)
```

## Nextcloud App

### Look at and Move Documents

```scala
// This trace logs into the app and then looks at and moves some documents.

val traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>> 
  Type(R.id.account_username, "22203"):>> Type(R.id.account_password, "12321qweqaz!") :>> 
  Click(R.id.buttonOK)
val traceAllow = (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000) )
val traceSeeAbout = Click("Documents") :>> Sleep(2000) :>> Click("About.odt") :>> 
  Sleep(2000) :>> Click("About.txt") :>> Sleep(2000) :>> ClickBack :>> ClickBack
val traceSeeHummingbird = Sleep(1500) :>> Click("Photos") :>> Click("Coast.jpg") :>>
  ClickBack :>> Click("Hummingbird.jpg") :>> ClickBack :>> ClickBack
val moveManual =  LongClick("Nextcloud Manual.pdf") :>> Sleep(2000) :>> ClickMenu :>>
  Click("Move") :>> Click("Documents") :>>Sleep(1000) :>> Click("Choose") :>> Sleep(2000)
val moveBackManual =  Click("Documents") :>> LongClick("Nextcloud Manual.pdf") :>> 
      ClickMenu:>> Sleep(2000) :>> Click("Move") :>> Click("Choose") :>> Sleep(5000)

traceLogin :>> traceAllow :>> traceSeeAbout :>> traceSeeHummingbird :>> moveManual :>> moveBackManual
```

### Log In and Crash on Rotation

```scala
"// This trace logs in, accepts permissions if possible, and then 
// crashes the application by rotating on the move screen.

val traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\") :>>
  Type(R.id.account_username, \"22203\") :>> Type(R.id.account_password, \"12321qweqaz!\"):>>
  Click(R.id.buttonOK)
val traceAllow = (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000)) 
val traceCrash = LongClick(\"Documents\") :>> ClickMenu :>> Click(\"Move\") :>> Rotate
traceLogin :>> traceAllow :>> traceCrash
```


## Chimptrainer

### Log In and Randomly Slide

```scala
// This logs into the application and swipes sliders 10 times.

val basicTrace = Sleep(1000) :>> Click(\"Begin\") :>> Type(\"username\",\"test\") :>> 
  Type(\"password\",\"test\") :>> Click(\"Login\") :>> Click(\"Swipe Testing\")
val lsSeekbar = List(R.id.seekBar, R.id.seekBar2, R.id.seekBar3)
val lsDirection:List[Orientation] = List(Left, Right)
def randomSwipe(times: Int, action: EventTrace): EventTrace ={
  val r = scala.util.Random
  times match {
    case 0 => action
    case _ => 
      randomSwipe(times-1, action :>> Swipe(lsSeekbar(r.nextInt(3)), lsDirection(r.nextInt(2))))
  }
}
randomSwipe(10, basicTrace)
```



### Log In and Crash on Countdown

```scala
// This trace logs in, and then crashes the app by finishing a countdown while on another screen.

val traceLogin = Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> 
  Type("password","test") :>> Click("Login")
val traceCount = Click("Countdowntimer Testing") :>> Click("10 seconds") :>> Sleep(10000)
val traceCountCrash = Click("5 seconds") :>> ClickBack :>> Sleep(5000)
traceLogin :>> traceCount :>> traceCountCrash
```
