# Docker container that runs chimpcheck web


- Build the docker image cuplv-android-emulator following the instruction at https://github.com/cuplv/android-docker.


- Build the docker image for chimpcheck
```docker build -t chimpcheck .```

- Run the container
```docker run -di -p 3000:3000 --name=chimpcheck chimpcheck```
