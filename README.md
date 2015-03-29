## KSUploader-server
KSUploader is a instant sharing tool.
This is the runnable server side. If you would know what the client can do please read the correspondent [readme](https://github.com/KSUploader/KSUploader-client).

### How to install
Actually simply run it one time to generate the `server.properties`: a config file.
To run the server you can use: `java -jar KSUploader_server.jar`, you can define some settings with the launch command: serverDomain, folder, pass, port including all of them, they will be automatically saved in the config file. Ex. `java -jar KSUploader_server.jar domain.com uploads myPass 3040`

### How to configure
Open and edit the `server.properties` file, then launch the server again.
It contains:
* `domain`: the domain that will be returned in the link to the client.
* `password`: the password of the server that is required during the authentication check.
* `port`: the port where the server will accept new connections.
* `folder`: the folder where the files will be stored, it will be included in the returned URL.

>Disclaimer: this is a project in early development stage. Since that it may change at any time.