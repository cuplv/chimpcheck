Script 1: 
```
import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+> Swipe(*, *) <+>
  Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+> SleepG(Gen.choose(500, 2000))
val g_Intr = Rotate
val relevantMonkey = g_GS <+> g_Intr
Repeat(10, relevantMonkey)
```

Script 2: 
```
import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+> Swipe(*, *) <+>
  Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+> SleepG(Gen.choose(500, 2000))
val g_Intr = Rotate
val relevantMonkey = g_GS <+> g_Intr
val login: TraceGen = Click(R.id.skip) *>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") *>> Type(R.id.account_username, "22203") *>>
  Type(R.id.account_password, "12321qweqaz!") *>>  Click(R.id.buttonOK)
login *>> Repeat(10, relevantMonkey)
```

Script 3:
```
import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+> Swipe(*, *) <+>
  Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+> SleepG(Gen.choose(500, 2000))
val g_Intr = Rotate
val relevantMonkey = g_GS <+> g_Intr
val login: TraceGen = Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>> Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)
login *>> Repeat(10, relevantMonkey)
```

Script 4: (Reuse Repeat if Necessary)
```
import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+> Swipe(*, *) <+>
  Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+> SleepG(Gen.choose(500, 2000))
val g_Intr = Rotate
val relevantMonkey = g_GS <+> g_Intr
val login: TraceGen = Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>> Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)
val permiss = isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)

login :>> permiss *>> Repeat(10, relevantMonkey)
```

Script 5:
```
val login: TraceGen = Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>> Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)
val newConfig: Seq[(Int, TraceGen)] = Seq(
  (30, EventTrace.trace(Click(*))),
  (30, EventTrace.trace(LongClick(*))),
  (30, EventTrace.trace(Rotate)),
  (30, EventTrace.trace(ClickMenu))
)
val permiss = isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)

login :>> permiss *>> SubservientGorilla(Sleep(500) :>> Skip)(GorillaConfig(10, newConfig))
```

Script 6:
```
// Log In and Crash on Rotation
// This trace logs in, accepts permissions if possible, and then 
// crashes the application by rotating on the move screen.

val traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>>
  Type(R.id.account_username, "22203") :>> Type(R.id.account_password, "12321qweqaz!"):>>
  Click(R.id.buttonOK)
val traceAllow = (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)) 
val traceCrash = LongClick("Documents") :>> ClickMenu :>> Click("Move") :>> Rotate
traceLogin :>> traceAllow :>> traceCrash
```