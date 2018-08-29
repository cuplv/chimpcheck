import React, {Component} from 'react';

var app_test = {
  'ChimpTrainer': ['trainer-1', 'trainer-2'],
  'Nextcloud': ['nextcloud-1', 'nextcloud-2'],
  'Kistenstapleln': ['kisten-1', 'kisten-2']
};

var test_explanations = {
  'trainer-1': 'This test logs into an application, and then plays around with sliders.',
  'trainer-2': 'This test logs into an application, and then demonstrates an IllegalStateException by going to another screen as a timer is counting down.',
  'kisten-1': 'This test clicks around the application 500 times. There is a pop-up, Turm, that we have to deal with by Clicking Back.',
  'kisten-2': 'This test demonstrates a crash by being on a different screen when a timer goes down.',
  'nextcloud-1': 'This test logs into a dummy account, and then plays around with some of the functionality of the NextCloud application',
  'nextcloud-2': 'This test logs into a dummy account, and then crashes the application by rotating on a page that doesn\'t handle rotation.'
}

var start_scripts = {
  'trainer-1': "// This logs into the ChimpTrainer application using a known username and password, and then goes to the swiping screen.\nval basicTrace = Sleep(1000) :>> Click(\"Begin\") :>> Type(\"username\",\"test\") :>> Type(\"password\",\"test\") :>>\n  Click(\"Login\") :>> Click(\"Swipe Testing\")\n// These are the three sliders.\nval lsSeekbar = List(R.id.seekBar, R.id.seekBar2, R.id.seekBar3)\n// These are the directions that the sliders can slide. (How we want to swipe)\nval lsDirection:List[Orientation] = List(Left, Right)\n// This randomly swipes the sliders a specified amount of times.\ndef randomSwipe(times: Int, action: EventTrace): EventTrace ={\n  val r = scala.util.Random\n  times match {\n    case 0 => action\n    case _ => randomSwipe(times-1, action :>> Swipe(lsSeekbar(r.nextInt(3)), lsDirection(r.nextInt(2))))\n  }\n}\n// This logs in and then does the random sliding 10 times.\nval traceGen = randomSwipe(10, basicTrace)",
  'trainer-2': "// This logs into the ChimpTrainer application using a known username and password.\nval traceLogin = Sleep(1000) :>> Click(\"Begin\") :>> Type(\"username\",\"test\") :>> Type(\"password\",\"test\") :>> Click(\"Login\")\n// This starts a countdown, and waits for that to end.\nval traceCount = Click(\"Countdowntimer Testing\") :>> Click(\"10 seconds\") :>> Sleep(10000)\n// This starts a countdown, and then moves to another screen.\nval traceCountCrash = Click(\"5 seconds\") :>> ClickBack :>> Sleep(5000)\nval traceGen = traceLogin :>> traceCount :>> traceCountCrash",
  'nextcloud-1': "// This section of the trace logs into the Nextcloud application using a known username and password.\nval traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\"):>> Type(R.id.account_username, \"22203\"):>> Type(R.id.account_password, \"12321qweqaz!\") :>> Click(R.id.buttonOK)\n// In some versions of the Android Application, there is a permission screen to get past; This just passes through the permission screen.\nval traceAllow = (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000) )\n// This navigates to the About.txt file in the Documents folder and takes a look at it, and then navigates back.\nval traceSeeAbout = Click(\"Documents\") :>> Sleep(2000) :>> Click(\"About.odt\") :>> Sleep(2000) :>> Click(\"About.txt\") :>> Sleep(2000) :>>ClickBack :>> ClickBack\n// This navigates to the Hummingbird.jpg picture in the Photos folder and takes a look at it, and then navigates back.\nval traceSeeHummingbird = Sleep(1500) :>> Click(\"Photos\") :>> Click(\"Coast.jpg\") :>> ClickBack :>> Click(\"Hummingbird.jpg\") :>> ClickBack :>> ClickBack \n// This selects the Nextcloud Manual and moves it into the documents folder.\nval moveManual =  LongClick(\"Nextcloud Manual.pdf\") :>> Sleep(2000) :>> ClickMenu :>> Click(\"Move\") :>> Click(\"Documents\") :>>Sleep(1000) :>> Click(\"Choose\") :>> Sleep(2000) \n// This selects the Nextcloud Manual and moves it back into the root directory.\nval moveBackManual =  Click(\"Documents\")  :>>LongClick(\"Nextcloud Manual.pdf\") :>> ClickMenu:>> Sleep(2000) :>> Click(\"Move\") :>> Click(\"Choose\") :>> Sleep(5000)\n\nval traceGen = traceLogin :>> traceAllow :>> traceSeeAbout :>> traceSeeHummingbird :>> moveManual :>> moveBackManual",
  'nextcloud-2': "// This section of the trace logs into the Nextcloud application using a known username and password.\nval traceLogin = Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\"):>> Type(R.id.account_username, \"22203\") :>> Type(R.id.account_password, \"12321qweqaz!\") :>> Click(R.id.buttonOK)\n// In some versions of the Android Application, there is a permission screen to get past; This just passes through the permission screen.\nval traceAllow = (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000)) \n// This demonstrates a crash within the application; If you go to the Move screen and immediately rotate the emulator, the application will crash.\nval traceCrash = LongClick(\"Documents\") :>> ClickMenu :>> Click(\"Move\") :>> Rotate\nval traceGen = traceLogin :>> traceAllow :>> traceCrash",
  'kisten-1': "//If we land on the \"Turm\" screen, then Click(*) won't work, so we need to go back to the previous screen.\nval checkTurm = Try((isDisplayed(\"Turm\") Then ClickBack:>>Skip).generator.sample.get)\n//This clicks randomly 500 times, unless it gets to the Turm screen, where it goes back a screen.\nval traceGen = Repeat(500, Click(*) :>> checkTurm) :>> Skip",
  'kisten-2': "//This is a fairly simple crash; We just head to the Countdown page, start a countdown, and then move to another screen.\nval traceGen = Click(\"Countdown\") :>> Click(\"0:10\") :>> Click(\"Countdown\") :>> Click(\"Punktzahl berechnen\") :>> Sleep(10000)"
}

var app_names = Object.keys(app_test)
var test_names = Object.values(app_test)
class Dropdown extends Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this)
    this.state = {
      appname: app_names[0],
      tests: test_names[0],
      test: this.getTestName(test_names[0][0]),
      written_test: start_scripts[this.getTestName(test_names[0][0])],
      results: "Test output",
      status: "",
      color: "#000000",
      trace: ""
    };
  }

  onChangeApp(e) {
    this.setState({
      appname: e.target.value,
      tests: app_test[e.target.value],
      test: this.getTestName(app_test[e.target.value][0]),
      written_test: start_scripts[this.getTestName(app_test[e.target.value][0])],
    })
    console.log(this.state)
  }
  getTestName(s) {
    if (s.indexOf(" ") > 0){
      return(s.substring(0, s.indexOf(" ")))
    }  
    return(s)
  }
  onChangeTest(e) {
    var testName = this.getTestName(e.target.value)
    this.setState({
      test: testName,
      written_test: start_scripts[testName]
    })
  }

  // From https://reactjs.org/docs/forms.html
  handleChange(e) {
    this.setState({written_test: e.target.value});
  }

  onClick(e) {
    var app = this.state.appname;
    var script = this.state.written_test;
    var original = this;
    this.setState({status:"Running"});
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
            const stat = json.getElementById('status').innerHTML
            const res = json.getElementById('eventTrace').innerHTML
            const trace = json.getElementById('stackTrace').innerHTML
            const col = json.getElementById('color').innerHTML
            original.setState({results:res, status: stat, trace:trace, color: col});
          } catch(err){
            original.setState({results:data, status: 'Unknown', trace:'', color: '#888888'})
          }
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
                <textarea className="w-100 p-7" rows="15" value={this.state.written_test} onChange ={this.handleChange}></textarea>
              </div>
            </div>
                <div className='row'>
                    <div className='col'>
                    <button id='testButton' className='btn-normal' onClick={this.onClick.bind(this)}> Test </button>
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
            <script>"document.getElementById('streamed').src = document.location.hostname + ':9002"</script>
            </div>
            <div className="col-4">
              <label>Executed Instructions</label>
              <textarea className="w-100 p-7" rows="6" readOnly style={{color: this.state.color}} value={this.state.results}></textarea>
              <div className='spaceySmall'></div>
              <label>Stack Trace (On Exception)</label>
              <textarea className="w-100 p-7" rows="10" readOnly></textarea>
            </div>
          </div>
        </div>
        );
  }
}
export default Dropdown;