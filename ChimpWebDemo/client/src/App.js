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
          <div className="App-title">
            <a style={{color: "#ffffff"}} href="http://plv.colorado.edu/chimpcheck">ChimpCheck</a>
          </div>
          <div className="App-project">
            <a style={{color: "#ffffff"}} href="http://plv.colorado.edu/chimpcheck/getting-started">Tutorial</a>
          </div>
        </header>
        <div className='somePad'></div>
        
            <Dropdown id="dd-app" parent="null" value="default" label="App Name" options={['ChimpTrainer', 'Nextcloud', 'Kistenstapleln']} />
            
      </div>
    );
  }
}

export default App;
