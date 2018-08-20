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
  'trainer-1': 'Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> Type("password","test") :>> Click("Login") :>> Click("Swipe Testing") :>> Swipe(2131427438,Right) :>> Swipe(2131427439,Left) :>> Swipe(2131427438,Right) :>> Swipe(2131427438,Left) :>> Swipe(2131427439,Right) :>> Swipe(2131427437,Left) :>> Swipe(2131427438,Right) :>> Swipe(2131427438,Left) :>> Swipe(2131427438,Right) :>> Swipe(2131427439,Left) :>> Swipe(2131427437,Right) :>> Swipe(2131427437,Left) :>> Swipe(2131427438,Right) :>> Swipe(2131427437,Left) :>> Swipe(2131427437,Right) :>> Swipe(2131427437,Left) :>> Swipe(2131427438,Right) :>> Swipe(2131427439,Left) :>> Swipe(2131427439,Right) :>> Swipe(2131427439,Left) :>> Skip :>> Skip :>> Sleep(5000) :>> ClickBack',
  'trainer-2': 'Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> Type("password","test") :>> Click("Login") :>> Click("Countdowntimer Testing") :>> Click("10 seconds") :>> Sleep(10000) :>> Click("5 seconds") :>> ClickBack :>> Sleep(5000)',
  'nextcloud-1': 'Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com"):>> Type(R.id.account_username, "22203"):>> Type(R.id.account_password, "12321qweqaz!") :>> Click(R.id.buttonOK) :>> (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000) ) :>> Click("Documents") :>> Sleep(2000) :>> Click("About.odt") :>> Sleep(2000)  :>> Click("About.txt") :>> Sleep(2000) :>>ClickBack :>> ClickBack :>> Sleep(1500) :>> Click("Photos") :>> Click("Coast.jpg") :>> ClickBack :>> Click("Hummingbird.jpg") :>> ClickBack :>> ClickBack :>> LongClick("Nextcloud Manual.pdf") :>> Sleep(2000) :>> ClickMenu :>> Click("Move") :>> Click("Documents") :>>Sleep(1000) :>> Click("Choose") :>> Sleep(2000) :>> Click("Documents")  :>>LongClick("Nextcloud Manual.pdf") :>> ClickMenu:>> Sleep(2000) :>> Click("Move") :>> Click("Choose") :>> Sleep(5000)',
  'nextcloud-2': 'Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com"):>> Type(R.id.account_username, "22203"):>> Type(R.id.account_password, "12321qweqaz!") :>> Click(R.id.buttonOK) :>> (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)) :>> LongClick("Documents") :>> ClickMenu :>> Click("Move") :>> Rotate',
  'kisten-1': 'Repeat(500, Click(*) :>> Try((isDisplayed("Turm") Then ClickBack:>>Skip).generator.sample.get)) :>> Skip',
  'kisten-2': 'Click("Countdown") :>> Click("0:10") :>> Click("Countdown") :>> Click("Punktzahl berechnen") :>> Sleep(10000)'
}

var app_names = Object.keys(app_test)
var test_names = Object.values(app_test)
class Dropdown extends Component {
  constructor(props) {
    super(props);
    this.state = {
      appname: app_names[0],
      tests: test_names[0],
      test: test_names[0][0],
      written_test: start_scripts[test_names[0][0]],
      results: "Test output"
    };
  }

  onChangeApp(e) {
    this.setState({
      appname: e.target.value,
      tests: app_test[e.target.value],
      test: app_test[e.target.value][0],
      written_test: start_scripts[app_test[e.target.value][0]],
    })
    console.log(this.state)
  }
  onChangeTest(e) {
    const testName = e.target.value.substring(0, e.target.value.indexOf(" "))
    this.setState({
      test: testName,
      written_test: start_scripts[testName]
    })
  }

  // From https://reactjs.org/docs/forms.html
  handleChange(event) {
    this.setState({value: event.target.value});
  }

  onClick(e) {
    var app = this.state.appname;
    var test = this.state.test;
    var original = this;
    fetch('/test', {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        appname: app,
        testname: test,
        toRun: 'setUp',
        UID: '0'
      })
    }).then(res => res.text()).then(function(uID){ 
      document.getElementById("streamed").src = document.location.href+"stream.html?"+uID;
      fetch('/test', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
        body: JSON.stringify({
            appname: app,
            testname: test,
            toRun: 'runADB',
            UID: uID
            })
        }).then(res => 
            res.text())
          .then(function(data){ 
            original.setState({results:data});
            fetch('/test', {
              method: 'POST',
              headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
              },
              body: JSON.stringify({
                appname: app,
                testname: test,
                toRun: 'tearDown',
                UID: uID
              })
            }).then(res => res.text()).then(function(data){
              document.getElementById("streamed").src = document.location.href+"empty.html"
            })
          })
    })
  }
  render() {
        return (
            <div>
            <label htmlFor='sel1'>Select an Application</label> 
            <select className="form-control" onChange={this.onChangeApp.bind(this)} id="sel1">
                {
                    app_names.map(option => {
                            return <option value={option} key={option} >{option}</option>})
                }
            </select>
            <label htmlFor='sel1'>Select a Test</label>
            <select className ='form-control' onChange = {this.onChangeTest.bind(this)} id = 'sel1'> {
                    this.state.tests.map(option => {
                        return <option value={option} key={option} >{option}</option>})
                    }
            </select>
            <div className='row'>
              <div className='col'>
                <textarea className="w-100 p-7" rows="4" value={this.state.written_test} onChange ={this.handleChange}></textarea>
              </div>
            </div>
                <div className='row'>
                    <div className='col'>
                    <button className='btn-normal' onClick={this.onClick.bind(this)}> Test </button>
                    </div>
                </div>
                <div className='row'>
                    <div className='col'>
                      <textarea className="w-100 p-7" rows="10" value={this.state.results} readOnly></textarea>
                    </div>
                </div>
            </div>

        );
  }
}
export default Dropdown;