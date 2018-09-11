#!/bin/bash

ROOTWEB="/chimpcheck/ChimpWebDemo"

cat ${ROOTWEB}/package.json | sed 's/localhost:9002/'"$STREAM_ADDRESS":"$STREAM_PORT"'/'  > ${ROOTWEB}/package2.json
cat ${ROOTWEB}/package2.json > ${ROOTWEB}/package.json
rm ${ROOTWEB}/package2.json

cat ${ROOTWEB}/client/src/Dropdown.js | sed 's/localhost:9002/'"$STREAM_ADDRESS":"$STREAM_PORT"'/' > ${ROOTWEB}/client/src/App2.js
cat ${ROOTWEB}/client/src/App2.js > ${ROOTWEB}/client/src/Dropdown.js
rm ${ROOTWEB}/client/src/App2.js

cat /minicap/example/public/index.html | sed 's/9002/'"$STREAM_PORT"'/' | sed 's/localhost/'"$STREAM_ADDRESS"'/' > /minicap/example/public/index2.html
cat /minicap/example/public/index2.html > /minicap/example/public/index.html
rm /minicap/example/public/index2.html
