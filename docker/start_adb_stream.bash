#!/bin/bash

# Following steps from github.com/openstf/minicap/example.
# This uses the default web server on minicap currently.
(cd /minicap && ./run.sh -P 320x480@320x480/0) & (cd /minicap/example && PORT=9002 node app.js) &
(sleep 20 && adb forward tcp:1717 localabstract:minicap) & wait