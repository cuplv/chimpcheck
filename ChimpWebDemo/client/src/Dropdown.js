import React, {Component} from 'react';

var app_test = {
  'ChimpTrainer': ['trainer-1', 'trainer-2'],
  'Nextcloud': ['nextcloud-1', 'nextcloud-2'],
  'Kistenstapleln': ['kisten-1', 'kisten-2']
};
var app_names = Object.keys(app_test)
var test_names = Object.values(app_test)
class Dropdown extends Component {
  constructor(props) {
    super(props);
    this.state = {
      appname: app_names[0],
      tests: test_names[0],
      test: test_names[0][0],
      results: "Test output"
    };
  }

  onChangeApp(e) {
    this.setState({
      appname: e.target.value,
      tests: app_test[e.target.value],
      test: app_test[e.target.value][0]
    })
    console.log(this.state)
  }
  onChangeTest(e) {
    this.setState({test: e.target.value})
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
      document.getElementById("streamed").src = document.location.href+"stream.html/"+uID;
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
            <label htmlFor='sel1'>Select one of the App</label> 
            <select className="form-control" onChange={this.onChangeApp.bind(this)} id="sel1">
                {
                    app_names.map(option => {
                            return <option value={option} key={option} >{option}</option>})
                }
            </select>
            <label htmlFor='sel1'>Select one of the test</label>
            <select className ='form-control' onChange = {this.onChangeTest.bind(this)} id = 'sel1'> {
                    this.state.tests.map(option => {
                        return <option value={option} key={option} >{option}</option>})
                    }
            </select>
                <div className='row'>
                    <div className='col'>
                    <button className='btn-normal' onClick={this.onClick.bind(this)}> Test </button>
                    </div>
                </div>
                <div className='row'>
                    <div className='col'>
                      <textarea className="w-100 p-3" value={this.state.results} readOnly></textarea>
                    </div>
                </div>
            </div>

        );
  }
}
export default Dropdown;