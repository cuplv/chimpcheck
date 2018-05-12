import React, { Component } from 'react';
import logo from './logo.svg';
import emulator from './emulator.jpeg';
import './App.css';
import Dropdown from './Dropdown'

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">ChimpCheck Web</h1>
        </header>
        <div id="container">
          <div className="row">
            <div className="col-4">
            <Dropdown id="dd-app" parent="null" value="default" label="App Name" options={['ChimpTrainer', 'Nextcloud', 'Kistenstapleln']} />
            <br/>

            </div>
            <div className="col-4">
            <img src={emulator} height="500" width="250" alt="logo" />
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default App;
