adb -s emulator-5554 uninstall plv.colorado.edu.chimptrainer
adb -s emulator-5554 uninstall plv.colorado.edu.chimptrainer.test
adb -s emulator-5554 install $(dirname "$0")/trainer/app-debug.apk
adb -s emulator-5554 install $(dirname "$0")/trainer/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -r -w -e debug false -e eventTrace CgsIARIHCAY6AwjoBwoTCAESDwgBEgsKCQgCGgVCZWdpbgocCAESGAgFMhQKDAgCGgh1c2VybmFtZRIEdGVzdAocCAESGAgFMhQKDAgCGghwYXNzd29yZBIEdGVzdAoTCAESDwgBEgsKCQgCGgVMb2dpbgokCAESIAgBEhwKGggCGhZDb3VudGRvd250aW1lciBUZXN0aW5nChgIARIUCAESEAoOCAIaCjEwIHNlY29uZHMKCwgBEgcIBjoDCJBOChcIARITCAESDwoNCAIaCTUgc2Vjb25kcwoGCAIaAggBCgsIARIHCAY6AwiIJw== -e appPackageName plv.colorado.edu.chimptrainer -e class plv.colorado.edu.chimptrainer.TestExpresso plv.colorado.edu.chimptrainer.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner
