#!/bin/bash

cat /chimpcheck/ChimpWebDemo/package.json | sed 's/9002/'"$STREAM_PORT"'/' > /chimpcheck/ChimpWebDemo/package2.json
cat /chimpcheck/ChimpWebDemo/package2.json > /chimpcheck/ChimpWebDemo/package.json
rm /chimpcheck/ChimpWebDemo/package2.json
cat /chimpcheck/ChimpWebDemo/client/src/Dropdown.js | sed 's/9002/'"$STREAM_PORT"'/' > /chimpcheck/ChimpWebDemo/client/src/App2.js
cat /chimpcheck/ChimpWebDemo/client/src/App2.js > /chimpcheck/ChimpWebDemo/client/src/Dropdown.js
rm /chimpcheck/ChimpWebDemo/client/src/App2.js
cat /minicap/example/public/index.html | sed 's/9002/'"$STREAM_PORT"'/' | sed "s/localhost/'+document.location.hostname+'/" > /minicap/example/public/index2.html
cat /minicap/example/public/index2.html > /minicap/example/public/index.html
rm /minicap/example/public/index2.html