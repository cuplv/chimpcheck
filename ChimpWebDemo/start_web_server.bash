#!/bin/bash

# Starts the server serving the requests to chimpcheck and the web server (on port 3001)
cd /chimpcheck/ChimpWebDemo && npm start &

cd /chimpcheck/ChimpWebDemo/client && npm start

while sleep 60; do
    echo "Monitoring..."
done
