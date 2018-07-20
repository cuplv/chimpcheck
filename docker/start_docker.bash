#!/bin/bash

# Start the web server! (This is for hardware acceleration)
bash /chimpcheck/ChimpWebDemo/start_web_server.bash & \
sudo env "PATH=$PATH" emulator -avd test -noaudio -no-boot-anim -gpu off & wait

# Start the web servier! (Not hardware acceleration)
# bash /chimpcheck/ChimpWebDemo/start_web_server.bash & \
# sudo env "PATH=$PATH" emulator -avd test -noaudio -no-boot-anim -gpu off -accel off & wait