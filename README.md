# noted-mc
this is an in-devlopment project that manages the multiple software compontents of running funny lolxd blockgame concert.

## project structure
the project is made up of three main components:
- discord bot layer
- api layer
- minecraft client layer

### api layer
the api layer acts as the intermediary between the user input from discord and the atually minecraft client interface.
it is a REST api, meaning that it is completly stateless, so the both the discord and minecraft layers have to manage their own states
independently. bceause of this, the api is incredibly simple to use and interface with. the api has only two routes, which provide all of its
functionality:
  - songs
  - queue

#### queue
the queue route implements the song queue. a GET from ```/queue``` returns a json array of the songs in the queue and
a GET from ```/queue/next``` returns the id of the next song and remove that song from the queue, 
while a POST to ```/queue/[valid song id here]``` will add that song to the back of the queue.

#### songs
the songs route implements all functionality relating concerning songs themselves. a GET from ```/songs``` returns a json array of all songs stored in the database.
each entry is made up of an ```_id```, ```name```, and ```datePosted```. a GET from ```/songs/[valid song id here]``` returns the MIDI file data for the given song.
to upload a song, POST to ```/songs``` with a form data containing a ```name``` and a ```file``` (a path to a valid MIDI file). to remove a song from the database,
DELETE from ```/songs/[valid song id here]```.
