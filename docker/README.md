# Docker container that runs chimpcheck web


- Build the docker image cuplv-android-emulator following the instruction at https://github.com/cuplv/android-docker.

	Make sure you're using the install-sbt branch. It's recommended to use x86 emulator instead of the ARM emulator, so before building, you may need to comment and uncomment lines accordingly.

```
RUN sdkmanager "system-images;android-23;default;x86_64"
RUN echo "no" | avdmanager create avd -n test -k "system-images;android-23;default;x86_64"

#RUN sdkmanager "system-images;android-23;google_apis;armeabi-v7a"
#RUN echo "no" | avdmanager create avd -n test -g google_apis -k "system-images;android-23;google_apis;armeabi-v7a"
```

- Build the docker image for chimpcheck
```docker build -t chimpcheck .```

- Run the container
```docker run -di --privileged -p 3001:3001 -p 5901:5901 --name=chimpcheck chimpcheck```

- The docker container will start a web server reachable on port 3001 with the web demo of chimpcheck (check http://localhost:3001 if deploying the docker container locally)
You will also be able to connect to the docker container with a VNC client in order to see the android emulator.
