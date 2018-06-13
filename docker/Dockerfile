FROM cuplv-android-emulator

RUN git clone https://github.com/cuplv/chimpcheck \
    && cd ./chimpcheck/ChimpDriver \
    && git submodule init \
    && git submodule update

RUN cd ./chimpcheck/ChimpDriver && ./gradlew uploadArchives
RUN cd ./chimpcheck/ScalaBashing && sbt publishLocal
RUN cd ./chimpcheck/ChimpCombinator && sbt publishLocal


# Install npm
RUN apt-get remove nodejs npm nodejs-legacy
RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
RUN sudo apt-get install -y nodejs
RUN sudo npm cache clean -f

RUN cd ./chimpcheck/ChimpWebDemo && npm install

EXPOSE 3000

WORKDIR ./chimpcheck/ChimpWebDemo
ENTRYPOINT ["npm", "start"]
