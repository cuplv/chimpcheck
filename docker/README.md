# Docker container that runs chimpcheck web


- Build the docker image cuplv-android-emulator following the instruction at https://github.com/cuplv/android-docker.

- Build the docker image for chimpcheck
```docker build -t chimpcheck .```

- Run the container with
```docker run -di --privileged -p 3001:3001 -p 9003:9002 --env STREAM_PORT=9003 -env STREAM_ADDRESS=localhost --name=chimpcheck chimpcheck```
if you want to run the Android Emulator off port 9003.

- The docker container will start a web server reachable on port 3001 with the web demo of chimpcheck (check http://localhost:3001 if deploying the docker container locally)
You will also be able to connect to the docker container with a VNC client in order to see the android emulator by connecting to 127.0.0.1:5901.
