FROM cuplv-android-emulator

# Install NDK (Android Size is 320x480)
RUN cd tmp && wget https://dl.google.com/android/repository/android-ndk-r17b-linux-x86_64.zip
RUN cd tmp && unzip android-ndk-r17b-linux-x86_64.zip -d /
RUN cd tmp && rm android-ndk-r17b-linux-x86_64.zip
ENV PATH $PATH:/android-ndk-r17b

# Install npm
RUN echo "Y" | apt-get remove nodejs npm nodejs-legacy
RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
RUN sudo apt-get install -y nodejs
RUN sudo npm cache clean -f

# Install minicap server
RUN git clone https://github.com/openstf/minicap
RUN cd ./minicap && git submodule update --init
RUN cd ./minicap && ndk-build
RUN npm install ws
RUN npm install express
EXPOSE 9002
RUN mkdir chimpRun


WORKDIR ./chimpRun
COPY start_docker.bash start_docker.bash
COPY ../docker/start_adb_stream.bash start_adb_stream.bash
ADD ../docker/dummy_chimpcheck.conf /etc/supervisor/conf.d
ENV STREAM_PORT 9002
ENTRYPOINT ["/usr/bin/supervisord"]
