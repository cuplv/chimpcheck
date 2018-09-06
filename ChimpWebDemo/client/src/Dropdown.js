import React, {Component} from 'react';
import codemirror from 'codemirror';

import "codemirror/mode/clike/clike.js";
import "codemirror/lib/codemirror.css";

var app_test = {
  'Kistenstapleln': ['Randomly Click', 'Crash on Countdown'],
  'Nextcloud': ['Look at and Move Documents', 'Log In and Crash on Rotation'],
  'ChimpTrainer': ['Log In and Randomly Slide', 'Log In and Crash on Countdown']
};

var start_scripts = {
  'Log In and Randomly Slide': "// This logs into the application and swipes sliders 10 times.\n\nval basicTrace = Sleep(1000) :>> Click(\"Begin\") :>> Type(\"username\",\"test\") :>> \n  Type(\"password\",\"test\") :>> Click(\"Login\") :>> Click(\"Swipe Testing\")\nval lsSeekbar = List(R.id.seekBar, R.id.seekBar2, R.id.seekBar3)\nval lsDirection:List[Orientation] = List(Left, Right)\ndef randomSwipe(times: Int, action: EventTrace): EventTrace ={\n  val r = scala.util.Random\n  times match {\n    case 0 => action\n    case _ => \n      randomSwipe(times-1, action :>> Swipe(lsSeekbar(r.nextInt(3)), lsDirection(r.nextInt(2))))\n  }\n}\nrandomSwipe(10, basicTrace)",
  'Log In and Crash on Countdown': "// This trace logs in, and then crashes the app by finishing a countdown while on another screen.\n\nval traceLogin = Sleep(1000) :>> Click(\"Begin\") :>> Type(\"username\",\"test\") :>> \n  Type(\"password\",\"test\") :>> Click(\"Login\")\nval traceCount = Click(\"Countdowntimer Testing\") :>> Click(\"10 seconds\") :>> Sleep(10000)\nval traceCountCrash = Click(\"5 seconds\") :>> ClickBack :>> Sleep(5000)\ntraceLogin :>> traceCount :>> traceCountCrash",
  'Look at and Move Documents': "// This trace logs into the app and then looks at and moves some documents.\n\nval traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\") :>> \n  Type(R.id.account_username, \"22203\"):>> Type(R.id.account_password, \"12321qweqaz!\") :>> \n  Click(R.id.buttonOK)\nval traceAllow = (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000) )\nval traceSeeAbout = Click(\"Documents\") :>> Sleep(2000) :>> Click(\"About.odt\") :>> \n  Sleep(2000) :>> Click(\"About.txt\") :>> Sleep(2000) :>> ClickBack :>> ClickBack\nval traceSeeHummingbird = Sleep(1500) :>> Click(\"Photos\") :>> Click(\"Coast.jpg\") :>>\n  ClickBack :>> Click(\"Hummingbird.jpg\") :>> ClickBack :>> ClickBack\nval moveManual =  LongClick(\"Nextcloud Manual.pdf\") :>> Sleep(2000) :>> ClickMenu :>>\n  Click(\"Move\") :>> Click(\"Documents\") :>>Sleep(1000) :>> Click(\"Choose\") :>> Sleep(2000)\nval moveBackManual =  Click(\"Documents\") :>> LongClick(\"Nextcloud Manual.pdf\") :>> \n      ClickMenu:>> Sleep(2000) :>> Click(\"Move\") :>> Click(\"Choose\") :>> Sleep(5000)\n\ntraceLogin :>> traceAllow :>> traceSeeAbout :>> traceSeeHummingbird :>> moveManual :>> moveBackManual",
  'Log In and Crash on Rotation': "// This trace logs in, accepts permissions if possible, and then \n// crashes the application by rotating on the move screen.\n\nval traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\") :>>\n  Type(R.id.account_username, \"22203\") :>> Type(R.id.account_password, \"12321qweqaz!\"):>>\n  Click(R.id.buttonOK)\nval traceAllow = (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000)) \nval traceCrash = LongClick(\"Documents\") :>> ClickMenu :>> Click(\"Move\") :>> Rotate\ntraceLogin :>> traceAllow :>> traceCrash",
  'Randomly Click': "// This randomly clicks 500 times; \n// If it ever reaches the Turm screen, it will go back to the previous screen.\n\nval checkTurm = \n  Try((isDisplayed(\"Turm\") Then ClickBack:>>Skip).generator.sample.get)\nRepeat(500, Click(*) :>> checkTurm) :>> Skip",
  'Crash on Countdown': "// This crashes the app by finishing a countdown on another page.\n\nClick(\"Countdown\") :>> Click(\"0:10\") :>> Click(\"Countdown\") :>> \n  Click(\"Punktzahl berechnen\") :>> Sleep(10000)"
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
