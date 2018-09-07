import React, {Component} from 'react';
import codemirror from 'codemirror';

import "codemirror/mode/clike/clike.js";
import "codemirror/lib/codemirror.css";

var app_test = {
  'Nextcloud': ['Randomly exercise interesting user interactions',
                'Express the user interactions necessary to succesfully login',
                'Remove the interruptible user interaction when logging in',
                'Express the user interaction to use when the App requests for user permissions',
                'Generate only a subset of relevant user interactions',
                'Concise test case to log in and crash the app',
                'Look at and Move Documents'],
  'Kistenstapleln': ['Randomly Click', 'Crash on Countdown'],
  'ChimpTrainer': ['Log In and Randomly Slide', 'Log In and Crash on Countdown']
};


var start_scripts = {
// Nextcloud
  'Randomly exercise interesting user interactions': `import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+>
  Swipe(*, *) <+> Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+>
  SleepG(Gen.choose(500, 2000))

val g_Intr = Rotate

val relevantMonkey = g_GS <+> g_Intr

Repeat(10, relevantMonkey)
`,
//
'Express the user interactions necessary to succesfully login' :`
import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random
val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+>
  Swipe(*, *) <+> Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+>
  SleepG(Gen.choose(500, 2000))

val g_Intr = Rotate

val relevantMonkey = g_GS <+> g_Intr

// User-input sequence to successfully login in the App
val login: TraceGen = Click(R.id.skip) *>>
  Type(R.id.hostUrlInput, "ncloud.zaclys.com") *>>
  Type(R.id.account_username, "22203") *>>
  Type(R.id.account_password, "12321qweqaz!") *>>  Click(R.id.buttonOK)

login *>> Repeat(10, relevantMonkey)
`,
//
  'Remove the interruptible user interaction when logging in' : `import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random

val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+>
  Swipe(*, *) <+> Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+>
  SleepG(Gen.choose(500, 2000))

val g_Intr = Rotate

val relevantMonkey = g_GS <+> g_Intr

val login: TraceGen = Click(R.id.skip) :>>
  Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>>
  Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)

login *>> Repeat(10, relevantMonkey)
`,
  'Express the user interaction to use when the App requests for user permissions' : `import org.scalacheck.Gen
import org.scalacheck.Gen._
import scala.util.Random

val rand = new Random()

def findCoord(): Coord = {
  val x = rand.nextInt(320)
  val y = rand.nextInt(240)
  Coord(x, y)
}

val g_GS = Click(*) <+> LongClick(*) <+> TypeG(const(*), Gen.alphaStr) <+>
  Swipe(*, *) <+> Pinch(findCoord(), findCoord(), findCoord(), findCoord()) <+>
  SleepG(Gen.choose(500, 2000))

val g_Intr = Rotate

val relevantMonkey = g_GS <+> g_Intr

val login: TraceGen = Click(R.id.skip) :>>
  Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>>
  Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)

// User interactions to allow Android permission
val permiss = isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)

login :>> permiss *>> Repeat(10, relevantMonkey)
`,
//
  'Generate only a subset of relevant user interactions' : `val login: TraceGen = Click(R.id.skip) :>>
  Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>>
  Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!") :>>  Click(R.id.buttonOK)

val relevantInteractions: Seq[(Int, TraceGen)] = Seq(
  (30, EventTrace.trace(Click(*))),
  (30, EventTrace.trace(LongClick(*))),
  (30, EventTrace.trace(Rotate)),
  (30, EventTrace.trace(ClickMenu))
)
val permiss = isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)

// Exercise only the relevant interactions
login :>> permiss *>>
  SubservientGorilla(Sleep(500) :>> Skip)(GorillaConfig(10, relevantInteractions))

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
`,
  //
  'Concise test case to log in and crash the app' : `// Log In and crash on phone rotation
// This trace logs in, accepts permissions if possible, and then 
// crashes the application by rotating on the move screen.

val traceLogin = Click(R.id.skip) :>>
  Type(R.id.hostUrlInput, "ncloud.zaclys.com") :>>
  Type(R.id.account_username, "22203") :>>
  Type(R.id.account_password, "12321qweqaz!"):>>
  Click(R.id.buttonOK)

val traceAllow = (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000))

val traceCrash = LongClick("Documents") :>> ClickMenu :>>
  Click("Move") :>> Rotate

traceLogin :>> traceAllow :>> traceCrash
`,
//
  'Look at and Move Documents' : `// This trace logs into the app and then looks at and moves some documents.

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

traceLogin :>> traceAllow :>> traceSeeAbout :>> traceSeeHummingbird :>> moveManual :>> moveBackManual`,
// Kistenstapeln
  'Randomly Click' : `// This randomly clicks 500 times;
// If it ever reaches the Turm screen, it will go back to the previous screen.

val checkTurm = 
  Try((isDisplayed("Turm") Then ClickBack:>>Skip).generator.sample.get)
Repeat(500, Click(*) :>> checkTurm) :>> Skip
`,
  //
  'Crash on Countdown' : `
// This crashes the app by finishing a countdown on another page.

Click("Countdown") :>> Click("0:10") :>> Click("Countdown") :>> 
  Click("Punktzahl berechnen") :>> Sleep(10000)
`,
// Chimptrainer
  'Log In and Randomly Slide' : `
// This logs into the application and swipes sliders 10 times.

val basicTrace = Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> 
  Type("password","test") :>> Click("Login") :>> Click("Swipe Testing")
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
`,
//
  'Log In and Crash on Countdown' : `
// This trace logs in, and then crashes the app by finishing a countdown while on another screen.

val traceLogin = Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> 
  Type("password","test") :>> Click("Login")
val traceCount = Click("Countdowntimer Testing") :>> Click("10 seconds") :>> Sleep(10000)
val traceCountCrash = Click("5 seconds") :>> ClickBack :>> Sleep(5000)
traceLogin :>> traceCount :>> traceCountCrash
`
}

var app_names = Object.keys(app_test)
var test_names = Object.values(app_test)
var cMirror = null;
class Dropdown extends Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this)
    this.state = {
      appname: app_names[0],
      tests: test_names[0],
      test: test_names[0][0],
      written_test: "// " + test_names[0][0] + "\n" + start_scripts[test_names[0][0]],
      results: "",
      status: "",
      color: "#000000",
      trace: ""
    };
  }

  onChangeApp(e) {
    this.setState({
      appname: e.target.value,
      tests: app_test[e.target.value],
      test: app_test[e.target.value][0],
      written_test: "// " + app_test[e.target.value][0] + "\n" + start_scripts[app_test[e.target.value][0]],
    })
    cMirror.setValue("// " + app_test[e.target.value][0] + "\n" + start_scripts[app_test[e.target.value][0]])
    console.log(this.state)
  }

  onChangeTest(e) {
    var testName = e.target.value
    this.setState({
      test: testName,
      written_test: "// " + testName + "\n" + start_scripts[testName]
    })
    cMirror.setValue("// " + testName + "\n" + start_scripts[testName])
  }

  // From https://reactjs.org/docs/forms.html
  handleChange(e) {
    this.setState({written_test: e.target.value});
    cMirror.setValue(e.target.value)
  }

  onClick(e) {
    var app = this.state.appname;
    var script = this.state.written_test;
    try{
      script = cMirror.getValue().substr(0); // Makes sure to only update script if getValue is a string.
    } catch(err){ console.log(err); };
    var original = this;
    this.setState({status:"Running", trace:"", results:""});
    document.getElementById('testButton').disabled = true;
    fetch('/test', {
      method: 'POST',
      headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
      body: JSON.stringify({
          appname: app,
          test: script,
          toRun: 'runADB'
          })
      }).then(res => 
          res.text())
        .then(function(data){ 
          document.getElementById('testButton').disabled = false
          try{
            var json = JSON.parse(data)
            const stat = json.status
            const res = json.eventTrace.replace(/ :>> /g, ":>>\n")
            const trace = json.stackTrace.replace(/at /g, "")
            const col = json.color
            original.setState({results:res, status: stat, trace:trace, color: col});
          } catch(err){
            original.setState({results:data, status: 'Unknown', trace:'', color: '#888888'})
          }
      })
  }
  
  componentDidMount(){
    cMirror = codemirror.fromTextArea(document.getElementById("input"), {
      mode: 'text/x-scala',
      
    })
  }  

  render() {
        return (
        <div id="container">
          <div className="row">
            <div className="col-4">
            <div>
            <div className='row'>
              <div className='col-6'>
                <label htmlFor='sel1'>App</label>
                <select className="form-control" onChange={this.onChangeApp.bind(this)} id="sel1">
                {
                    app_names.map(option => {
                            return <option value={option} key={option} >{option}</option>})
                }
                </select>
               </div>
               <div className='col-6'>
                 <label htmlFor='sel1'>Test</label>
                 <select className ='form-control' onChange = {this.onChangeTest.bind(this)} id = 'sel1'> {
                    this.state.tests.map(option => {
                        return <option value={option} key={option} >{option}</option>})
                    }
                 </select>
               </div>
            </div>
            <div className="spaceySmall"></div>
            <div className='row'>
              <div className='col'>
                <textarea id="input" className="w-100 p-7" rows="15" value={this.state.written_test} onChange ={this.handleChange} spellCheck="false"></textarea>
              </div>
            </div>
            <div className="somePad"></div>
            <div className="somePad"></div>
                <div className='row'>
                    <div className='col'>
                    <button id='testButton' className='btn-normal' onClick={this.onClick.bind(this)}> Generate </button>
                    </div>
                </div>
            </div>
            <br/>

            </div>
            <div className="col-4">
            <label>Status:</label>
            <textarea rows="1" cols="14" readOnly value={this.state.status}></textarea>
            <div className='nl'></div>
            <iframe src="http://localhost:9002" id="streamed" title="streamed" width="360" height="520" frameBorder="0"></iframe>
            </div>
            <div className="col-4">
              <label>Executed Instructions</label>
              <textarea className="w-100 p-7" rows="9" readOnly style={{color: this.state.color}} value={this.state.results}></textarea>
              <div className='spaceySmall'></div>
              <label>Stack Trace (On Exception)</label>
              <textarea className="w-100 p-7" rows="10" readOnly value={this.state.trace}></textarea>
            </div>
          </div>
        </div>
        );
  }
}
export default Dropdown;
