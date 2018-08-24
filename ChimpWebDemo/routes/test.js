var express = require('express');
var router = express.Router();
var appMap = {"ChimpTrainer":"trainer", "Nextcloud":"nextcloud", "Kistenstapleln":"kisten"};
/* GET users listing. */
const util = require('util');
const exec = util.promisify(require('child_process').exec);
router.post('/', function(req, res, next) {
  console.log(req.body)
  var reqBody = req.body
  console.log(appMap[reqBody.appname])
  var appName = appMap[reqBody.appname]
  var test = reqBody.test.replace(/"/g, '\\\"').replace(/\n/, '\\n')
  //console.log(appName,test)
  //var scriptPath = __dirname + '/../example/' + testName + '.sh'
  //var scriptPath = __dirname + '/../example/runCommand.sh ' + appName
  //console.log(scriptPath)
  async function ls() {
    // This is only temporarily here.
    const { stdout, stderr } = await exec('curl -X POST -d \'{"test": "'+appMap[reqBody.appname]+'", "eventTrace": "'+test+'"}\' http://localhost:18010/runADB')
    onsole.log('stdout:', stdout);
    res.send(stdout);
  }
  ls()

});

module.exports = router;
