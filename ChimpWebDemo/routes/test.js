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
  var testName = reqBody.testname
  console.log(appName,testName)
  //var scriptPath = __dirname + '/../example/' + testName + '.sh'
  var scriptPath = __dirname + '/../example/runCommand.sh ' + appName
  console.log(scriptPath)
  async function ls() {
    const { stdout, stderr } = await exec('bash ' +scriptPath + ' $( cat /../example/' + testName + '-eventTrace.txt )');
    console.log('stdout:', stdout);
    console.log('stderr:', stderr);
    res.send(stdout);
  }
  ls()

});

module.exports = router;
