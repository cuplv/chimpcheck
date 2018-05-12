adb -s emulator-5554 uninstall de.d120.ophasekistenstapeln
adb -s emulator-5554 uninstall de.d120.ophasekistenstapeln.test
adb -s emulator-5554 install $(dirname "$0")/kisten/app-debug.apk
adb -s emulator-5554 install $(dirname "$0")/kisten/app-debug-androidTest.apk
adb push $(dirname "$0")/kisten_command.sh /data/local/tmp
adb shell sh /data/local/tmp/kisten_command.sh
