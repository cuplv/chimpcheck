app1=$(cat $(dirname "$0")/${1}Info.txt | head -n 1)
app2=$(cat $(dirname "$0")/${1}Info.txt | tail -n 1)
adb -s emulator-5554 uninstall $app1
adb -s emulator-5554 uninstall ${app1}.test
adb -s emulator-5554 install $(dirname "$0")/$1/app-debug.apk
adb -s emulator-5554 install $(dirname "$0")/$1/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -r -w -e debug false -e eventTrace $2 -e appPackageName $app2 -e class $app2.TestExpresso $app1.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner
