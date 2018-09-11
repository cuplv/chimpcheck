#app1=$(cat $(dirname "$0")/${1}Info.txt | head -n 1)
#app2=$(cat $(dirname "$0")/${1}Info.txt | tail -n 1)
app1=$5
app2=$4
adb -H $1 -P $2 -s emulator-5554 uninstall $app1
adb -H $1 -P $2 -s emulator-5554 uninstall ${app1}.test
adb -H $1 -P $2 -s emulator-5554 install $(dirname "$0")/$3/app-debug.apk
adb -H $1 -P $2 -s emulator-5554 install $(dirname "$0")/$3/app-debug-androidTest.apk
adb -H $1 -P $2 -s emulator-5554 shell am instrument -r -w -e debug false -e eventTrace $6 -e appPackageName $app2 -e class $app2.TestExpresso $app1.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner
