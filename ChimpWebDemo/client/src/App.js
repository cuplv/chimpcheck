import React, { Component } from 'react';
import logo from './chimpLogo.png';
//import emulator from './emulator.jpeg';
import './App.css';
import Dropdown from './Dropdown'

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">ChimpCheck</h1>
          <div className="App-project">
            <a style={{color: "#ffffff"}} href="http://plv.colorado.edu/chimpcheck">Click to go to Project Page</a>
          </div>
        </header>
        <div className='somePad'></div>
        <div id="container">
          <div className="row">
            <div className="col-4">
            <Dropdown id="dd-app" parent="null" value="default" label="App Name" options={['ChimpTrainer', 'Nextcloud', 'Kistenstapleln']} />
            <br/>

            </div>
            <div className="col-4">
            <label>Status:</label>
            <textarea rows="1" cols="14" readOnly></textarea>
            <div className='nl'></div>
            <iframe src="http://localhost:9003" id="streamed" title="streamed" width="360" height="520" frameBorder="0"></iframe>
            <script>"document.getElementById('streamed').src = document.location.hostname + ':9003"</script>
            </div>
            <div className="col-4">
              <label>Executed Instructions</label>
              <textarea className="w-100 p-7" rows="6" readOnly value={"Test"}></textarea>
              <div className='spaceySmall'></div>
              <label>Stack Trace (On Exception)</label>
              <textarea className="w-100 p-7" rows="10" readOnly></textarea>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default App;
