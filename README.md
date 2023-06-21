# noted-mc
This is an in-devlopment project that manages the multiple software compontents of running funny lolxd blockgame concert.

## Project Structure
the project is made up of three main components:
- Discord Bot layer
- API layer
- Minecraft Client layer

### Discord Bot layer
The Discord Bot layer acts as the frontend for the project. Behind the scenes, the Discord Bot is feeds all of its user input to the API layer. Once you add the bot to a server, users can interact with the bot using slash-commands.
These commands are:
- /help
- /list
- /add
- /queue
- /now

#### /help
This command displays information about the bot and its purpose, as well as how to interface with it.

#### /list
This command displays a list of all of the songs the bot can play.

#### /add [song]
This command adds a song to the song queue. Song names are case-sensitive and should match the output from /list

#### /queue
This command displays the current state of the song queue. Higher numbers will be played sooner.

#### /now
This command displays the name of the currently playing song.

### API layer
The API layer acts as the intermediary between the user input from Discord and the actual Minecraft Client interface.
It is a REST API, meaning that it is completly stateless. Because of this, both the Discord and Minecraft Client layers have to manage their own states
independently. This makes the API incredibly simple to use and interface with. The API has only two routes, which provide all of its
functionality:
- songs
- queue

#### queue
The queue route implements the song queue. a GET from ```/queue``` returns a json array of the songs in the queue and
a GET from ```/queue/next``` returns the id of the next song and remove that song from the queue, 
while a POST to ```/queue/[valid song id here]``` will add that song to the back of the queue.

#### songs
The songs route implements all functionality relating to songs themselves. An HTTP GET from ```/songs``` returns a JSON array of all songs stored in the database.
Each entry consists of an ```_id```, ```name```, and ```datePosted```. An HTTP GET from ```/songs/[valid song _id here]``` returns the MIDI file data for the given song.
To upload a song, make an HTTP POST request to ```/songs``` with form data body containing ```name=[song name here]``` and ```file=@"[path to midi file]```. To remove a song from the database,
HTTP DELETE from ```/songs/[valid song _id here]```.

### Minecraft Client layer
TODO

## How to Use This
Truth be told, this code is probably not very useful to most people who find it. It is only open-source because that is just what I do for any serious programming projects I make. 
The MIDI to noteblock conversion code may be interesting to some (![MidiConverter.java](/client/src/main/java/net/nimrod/noted/converters/MidiConverter.java)), as well as how the 
client maps the noteblocks in its enviroment, tunes, and then plays them (![Noted.java](/client/src/main/java/net/nimrod/noted/Noted.java)). God forbid you do want to use this repo directly.
    
1. Deploy the API layer:        
    - Create a MongoDB instance using something like ![MongoDB Atlas](https://www.mongodb.com/atlas/database). Use the free tier (M0), create a new project, create a cluster
        for that project, and then create a database and collection in that cluster. This is where song schemas will be stored.
    - If you want the application to be running 24/7, you will need to deploy it on a server, which means you will need to run the following steps on said server.
    - Create a ```.env``` file in the ![api](/api) directory; inside of it, create the following enviroment variables:
        - ```MONGODB_USER="[your MongoDB account username]"```
        - ```MONGODB_PASSWORD="[your MongoDB account password]"```
        - ```MONGODB_CLUSTER="[name of your MongoDB cluster]"```
        - ```MONGODB_DB="[name of your MongoDB cluster's database]"```
        - ```PORT="[the port you want the API to run on (default is 3000)]"```
    - Run ```node src/index.js```, and your application will start running. If an error occurs, check that your MongoDB enviroment variables are present and correct.
2. Deploy the Discord Bot layer:
    - Create a new ![Discord application](https://discord.com/developers/applications). Give it a sensible name, description, etc.
    - Create and configure the application's bot with a name, icon, etc. Copy the token somewhere for later use.
    - Ensure that under "Privileged Gateway Intents", all intent options are enabled and that under "Bot Permissions", "Send Messages" is enabled. Save your changes.
    - Under OAuth2->URL Generator, select the scopes ```applications.commands``` and ```bot```.
    - Copy the generated URL and paste it into a text channel of the Discord server you want to add the bot to. Confirm that the configuration is correct, and add it to your server.
    - Again, if you want the bot to be running 24/7, you will need to deploy it on a server, which means you will need to run the following steps on said server.
    - Create a ```.env``` file in the ![discord-bot](/discord-bot) directory; inside of it, create the enviroment variable, ```DISCORD_TOKEN="[the bot's token which you copied earlier]"```
    - Run ```node src/index.js```, and you will see the bot connect and start running. You can now test it in the Discord server.
3. Build and run the Minecraft Client:
    - We are going to build the client from source so, you need Java as well as the JDK (version >= 17).
    - Open a terminal/command prompt in the ![client](/client) directory.
    - Run, Linux/OSX: ```./gradlew build``` or Windows: ```./gradlew.bat build```. This will take a while, but it should finish without any errors.
    - Copy the file ```noted-1.0.0.jar```from ```build/libs/``` to the ```mods``` directory of wherever your Minecraft Client folder is. You also need ```fabric-api``` in order to run the client.
    - Install ```fabric``` for Minecraft 1.19.4 if you don't already have it, and then run the game.
