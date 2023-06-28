# noted-mc
This is an in-development project that manages the multiple software components
of running goofy blockgame concert on the best anarchy server ever, [6b6t.org](https://6b6t.org).

## Project Structure
The project is made up of three main components:
- Discord Bot layer
- API layer
- Minecraft Client layer

### Discord Bot layer
The Discord Bot layer acts as the frontend for the project. Behind the scenes, the Discord Bot feeds all of the user input back to the API layer.
Once you add the bot to a server, users can interact with it using slash-commands.
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
This command adds a song to the song queue. Song names are case-sensitive and should match the output from `/list`

#### /queue
This command displays the current state of the song queue.

#### /now
This command displays the name of the currently playing song.

### API layer
The API layer acts as the intermediary between the user input from the Discord Bot and the Minecraft Client interface.
It is a REST API, meaning that it is completly stateless. Because of this, both the Discord Bot and Minecraft Client layers have to manage their own states
independently. This makes the API incredibly simple to use and interface with. The API has only two routes, which provide all of its
functionality:
- songs
- queue

#### songs
The songs route implements all functionality relating to songs themselves. An HTTP GET from `/songs` returns a JSON array of all songs stored in the database.
Each song entry consists of an `_id`, `name`, and `datePosted`. An HTTP GET from `/songs/[valid song _id here]` returns the MIDI file data for the given song.
To upload a song, HTTP POST to `/songs` with a form data body containing `name=[song name here]` and `file=@"[path to midi file]`.
To remove a song from the database, HTTP DELETE from `/songs/[valid song _id here]`.

#### queue
The queue route implements the song queue. a HTTP GET from `/queue` returns a JSON array of the songs in the queue and
a HTTP GET from `/queue/next` returns the song \_id of the next song and removes that song from the queue.
To add a song to the back of the queue, HTTP POST to `/queue/[valid song _id here]`.

### Minecraft Client layer
The Minecraft Client layer is how songs actually get played ingame. When activated, the client fully automates the process of downloading a song,
mapping surrounding noteblocks, tuning noteblocks, playing the song, and then repeating for every song in the queue. The client also features
a simple UI that displays whether the client is active or not, as well as the name of the currently playing song.
It is important to note (haha, get it?) that you must construct a noteblock machine before activating the client,
as it only scans for noteblocks around you. Try to include a variety a instruments, such as bass, flute, harp, chime, and guitar.
When activated, the client polls the API's `/queue/next` route for the song \_id of the next song. Once it gets the song \_id,
the client downloads that song from the API's `/songs` route into memory. Once the client has a song downloaded it begins to map the enviroment around it
for noteblocks. It takes into account both the unique pitches and instruments of every song, and maps them to the surrounding noteblocks as
best as possible. The client then tunes the noteblocks to the required pitches and begins playing. When the client finishes playing a song,
it automatically goes back to polling the API for the next one. The client also features a small command interface that allows one to do
things like `.skip` a song, `.pause` a song, `.toggle` the client on or off, etc. Use `.help` for a list of commands,
as well as details about a particular command.

## How to actually use this
Truth be told, this code is probably not very useful to most people who find it.
It is only open-source because that is just what I do for any projects I create that I am somewhat proud of.
The MIDI to noteblock conversion code may be interesting to some ([MidiConverter.java](/client/src/main/java/net/nimrod/noted/converters/MidiConverter.java)), as well as how the 
client maps the noteblocks in its enviroment, tunes, and then plays them ([SongPlayer.java](/client/src/main/java/net/nimrod/noted/playing/SongPlayer.java)).
God forbid you do want to use this repo directly, you should read ALL of the source code first to make sure I am not stealing any of your Discord info or trying to RAT your PC.
I'm not, but don't take my word and just read the damn source code first. Once you have done that, (try to) follow these steps:
    
1. Deploy the API layer:        
    - Create a MongoDB instance using something like [MongoDB Atlas](https://www.mongodb.com/atlas/database). Go through all of the steps needed to create a project, database, user/password, etc.
    - Select your driver as "Node.js" and copy the connection string somewhere for later use. Make sure you add the IP address of the computer/server you are going to connect to the database from to the access list.
    - If you want the application to be running 24/7, you will need to deploy it on a server, which means you will need to run the following steps on said server.
    - Create a `.env` file in the [api](/api) directory; inside of it, create the following enviroment variables:
        - `MONGODB_URI=[the connection string you saved from earlier. Replace <password> with your user's password. Right after mongodb.net/, put the name of your database]`
        - `PORT=[the port you want the API to run on (default is 3000)]`
    - Run `npm i` to install dependencies.
    - Run `node src/index.js`, and your application will start running. If an error occurs, check that your MongoDB connection string is correct.
2. Deploy the Discord Bot layer:
    - Create a new [Discord application](https://discord.com/developers/applications). Give it a sensible name, description, etc.
    - Create and configure the application's bot with a name, icon, etc. Copy the token somewhere for later use.
    - Enable all options under "Privileged Gateway Intents".
    - Under OAuth2->URL Generator, select the scopes `applications.commands` and `bot`. Under "Bot Permissions", enable "Send Messages".
    - Copy the generated URL and paste it into a text channel of the Discord server you want to add the bot to. Confirm that the configuration is correct, and add it to your server.
    - Again, if you want the bot to be running 24/7, you will need to deploy it on a server, which means you will need to run the following steps on said server.
    - Create a `.env` file in the [discord-bot](/discord-bot) directory; inside of it, create the following enviroment variables:
        - `API_URL=[the url/ip to the server running the API instance that we set up earlier]`
        - `DISCORD_TOKEN=[the bot's token which you copied earlier]`
        - `DISCORD_CLIENT_ID=[the bot's client id from discord]`
        - `DISCORD_GUILD_ID=[your server's guild id]`
    - Run `npm i` to install dependencies.
    - Run `node src/index.js`, and you will see the bot connect and start running. You can now test it in the Discord server.
3. Build and run the Minecraft Client:
    - We are going to build the client from source so, you need Java as well as the JDK (version >= 17).
    - Open a terminal/command prompt in the [client](/client) directory.
    - Run, Linux/OSX: `./gradlew build` or Windows: `./gradlew.bat build`. This might take a while, but it should finish without any errors.
    - Copy the file `noted-1.0.0.jar` from `build/libs/` to the `mods` directory of wherever your Minecraft Client folder is. You will also need `fabric-api`.
    - Install `fabric` for Minecraft 1.19.4 if you don't already have it, and then run the game.

## Coming soon
- Possible GUI interface for the Minecraft Client.
- Update the Minecraft Client to 1.20.1.
