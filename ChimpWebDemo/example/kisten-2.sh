adb -s emulator-5554 uninstall de.d120.ophasekistenstapeln
adb -s emulator-5554 uninstall de.d120.ophasekistenstapeln.test
adb -s emulator-5554 install $(dirname "$0")/kisten/app-debug.apk
adb -s emulator-5554 install $(dirname "$0")/kisten/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -r -w -e debug false -e eventTrace ChcIARITCAESDwoNCAIaCUNvdW50ZG93bgoSCAESDggBEgoKCAgCGgQwOjEwChcIARITCAESDwoNCAIaCUNvdW50ZG93bgohCAESHQgBEhkKFwgCGhNQdW5rdHphaGwgYmVyZWNobmVuCgsIARIHCAY6AwiQTg== -e appPackageName de.d120.ophasekistenstapeln -e class de.d120.ophasekistenstapeln.TestExpresso de.d120.ophasekistenstapeln.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner
