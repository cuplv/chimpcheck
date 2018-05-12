adb uninstall com.nextcloud.client.test                                                                            
adb uninstall com.nextcloud.client
adb -s emulator-5554 install $(dirname "$0")/nextcloud/NextCloud_Android-debug.apk
adb -s emulator-5554 install $(dirname "$0")/nextcloud/NextCloud_Android-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -r -w -e debug false -e eventTrace ChIIARIOCAESCgoICAEQwYPA+AcKJQgBEiEIBTIdCggIARD8gMD4BxIRbmNsb3VkLnphY2x5cy5jb20KGQgBEhUIBTIRCggIARCFgcD4BxIFMjIyMDMKIAgBEhwIBTIYCggIARCHgcD4BxIMMTIzMjFxd2VxYXohChIIARIOCAESCgoICAEQiYHA+AcKSggHQkYKIAgBEhwIARIYCgtpc0Rpc3BsYXllZBIJCAIaBUFsbG93EiIKEwgBEg8IARILCgkIAhoFQWxsb3cKCwgBEgcIBjoDCOgHCgYIARICCAcKFwgBEhMIAhoPCg0IAhoJRG9jdW1lbnRzCgYIAhoCCAMKEggBEg4IARIKCggIAhoETW92ZQoGCAIaAggG -e appPackageName com.owncloud.android -e class com.owncloud.android.TestExpresso com.nextcloud.client.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner
