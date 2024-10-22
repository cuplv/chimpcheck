FROM cuplv-android-emulator

# Install npm
RUN echo "Y" | apt-get remove nodejs npm nodejs-legacy
RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
RUN sudo apt-get install -y nodejs
RUN sudo npm cache clean -f

# Install NDK (Android Size is 320x480)
RUN cd tmp && wget https://dl.google.com/android/repository/android-ndk-r17b-linux-x86_64.zip
RUN cd tmp && unzip android-ndk-r17b-linux-x86_64.zip -d /
RUN cd tmp && rm android-ndk-r17b-linux-x86_64.zip
ENV PATH $PATH:/android-ndk-r17b

# Install minicap server
RUN git clone https://github.com/openstf/minicap
RUN cd ./minicap && git submodule update --init
RUN cd ./minicap && ndk-build
RUN npm install ws
RUN npm install express


RUN git clone https://github.com/cuplv/chimpcheck
RUN cd chimpcheck && git checkout demo_polish_frontend
RUN cd chimpcheck && git submodule init \
    && git submodule update

RUN cd ./chimpcheck/ChimpDriver && ./gradlew uploadArchives
RUN cd ./chimpcheck/ScalaBashing && sbt publishLocal
RUN cd ./chimpcheck/ChimpCombinator && sbt publishLocal
RUN cd ./chimpcheck/ChimpWebDemo && npm install
RUN cd ./chimpcheck/ChimpWebDemo/client && npm install

# Make sure sbt is loaded for the Backend
RUN cd ./chimpcheck/ChimpDemoSrv/ChimpCheckEmulatorService && sbt compile

EXPOSE 3001
EXPOSE 9002

#Get Ports Exposed for WebSocket Forwarder
# EXPOSE 19002

WORKDIR ./chimpcheck/ChimpWebDemo
COPY start_web_server.bash start_web_server.bash
COPY start_docker.bash start_docker.bash
COPY start_adb_stream.bash start_adb_stream.bash
COPY replacements.bash replacements.bash
ADD chimpcheck.conf /etc/supervisor/conf.d
RUN cd /chimpcheck && git pull
RUN cd /chimpcheck && git checkout demo_polish_frontend

ENV STREAM_PORT 9002
ENV STREAM_ADDRESS localhost

ENTRYPOINT ["/usr/bin/supervisord"]
