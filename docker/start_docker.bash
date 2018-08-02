#!/bin/bash

# Start the web server! (This is for hardware acceleration)
bash /chimpcheck/ChimpWebDemo/start_web_server.bash & \
(sleep 10 && sudo env "PATH=$PATH" emulator -avd test -noaudio -no-boot-anim -gpu swiftshader_indirect) & \
bash /chimpcheck/ChimpWebDemo/start_adb_stream.bash & wait

# Start the web server! (Not hardware acceleration)
# bash /chimpcheck/ChimpWebDemo/start_web_server.bash & \
# sudo env "PATH=$PATH" emulator -avd test -noaudio -no-boot-anim -gpu off -accel off & wait