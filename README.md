# URL Shortener - based on AKA protocol, Ethereum blockchain and Kotlin JS

This is a URL Shortener which showcase the [AKA protocol](https://akap.me) on the Ethereum blockchain.

## Install

You will need [Kotlin 1.3.61](https://github.com/JetBrains/kotlin/releases/tag/v1.3.61). You can download it independently
or install it as part of [IntelliJ IDEA](https://www.jetbrains.com/idea/).

To build and run the project, you need [gradle](https://gradle.org/downloads). Both gradle project file and wrapper Jars
are included in the project repo.

All dependencies are configured in gradle project. 

Download and install [Truffle Ganache](https://www.trufflesuite.com/ganache) to start a local Ethereum node.

Download and install [MetaMask](https://www.metamask.io) browser plugin to interact with the Etheruem blockchain from the app.

## Try

Start Ganache development network

Deploy AKAP [contract](https://github.com/cfelde/AKAP/tree/master/contracts) and URLShortener [contract](https://github.com/mohamedelshami/AKAP-url-shortener-contracts):

    $ truffle migrate --network development 

Make sure to update address as required within `src/main/web/contracts/AKAP.json`

Start webpack dev server with gradle run task:

    $ ./gradlew -t run

Start your browser at:

    $ http://localhost:8088/

## See it Live

https://redir.eth
