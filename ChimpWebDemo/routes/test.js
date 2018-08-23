var express = require('express');
var router = express.Router();
var appMap = {"ChimpTrainer":"trainer", "Nextcloud":"nextcloud", "Kistenstapleln":"kisten"};
/* GET users listing. */
const util = require('util');
const exec = util.promisify(require('child_process').exec);
router.post('/', function(req, res, next) {
  console.log(req.body)
  var reqBody = req.body
  var appName = appMap[reqBody.appname]
  var test = reqBody.test.replace('"', '\\"')
  console.log(appName,test)
  //var scriptPath = __dirname + '/../example/' + testName + '.sh'
  var scriptPath = __dirname + '/../example/runCommand.sh ' + appName
  console.log(scriptPath)
  async function ls() {
    // This is only temporarily here.
    //const { stdout, stderr } = await exec('bash ' +scriptPath + ' $( cat ' + __dirname + '/../example/' + testName + '-eventTrace.txt )' + ' '+ reqBody.toRun+' '+reqBody.UID);
    const stdout = await exec('curl -X POST -d \'{"test": "'+ reqBody.appName +'", "eventTrace": "'+reqBody.test+'"}\' http://localhost:18010')
    //const { stdout, stderr } = await exec('bash ' +scriptPath + ' $( echo "' + test.replace(/"/g, '\\\\"') + '" )' + ' '+ reqBody.toRun+' '+reqBody.UID);
    console.log('stdout:', stdout);
    //console.log('stderr:', stderr);
    // const stdout = $.post("localhost:18010", '{"test": "'+appName+'", '+eventTrace+'}', "")
    /*const stdout = fetch('localhost:18010', {
      method: 'POST',
      body: '{"test": "'+appName+'", "eventTrace": "'+eventTrace+'"}'
    }).then(reponse => reponse.body)*/
    res.send(stdout);
  }
  ls()

});

module.exports = router;
