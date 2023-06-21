const { Colors, EmbedBuilder, SlashCommandBuilder } = require("discord.js");
const axios = require("axios");

module.exports = {
    data: new SlashCommandBuilder()
    .setName("queue")
    .setDescription("List the songs in the queue"),

    async execute(interaction, client) {
        const queueData = await axios.get(`${process.env.NOTED_API_URL}/queue`);
        const songData = await axios.get(`${process.env.NOTED_API_URL}/songs`);

        if (queueData.status === 204) {
            return await interaction.reply({
                content: "Song queue is empty",
                ephemeral: true
            });
        }

        const currentSong = await axios.get(`${process.env.NOTED_API_URL}/queue/now`);

        let currentSongName;

        songData.data.songs.forEach((song) => {
            if (currentSong.data._id === song._id) {
                currentSongName = `${song.name}`;
            }
        });
        
        if (currentSongName == null) {
            currentSongName = "";
        }

        let songList = [];
        let i = 1;

        queueData.data.queue.forEach((item) => {
            songData.data.songs.forEach((song) => {
                if (item === song._id) {
                    songList.push(`**${i}.** ${song.name}`);
                }
            });

            i++;
        });

        await interaction.reply({
            embeds: [
                new EmbedBuilder().setTitle("Song Queue")
                                  .setColor(Colors.Blue)
                                  .setDescription("**Now Playing:** " + currentSongName + "\n    " + songList.join("\n   "))
                                  .setFooter({ text: "noted-mc v1.0.0" })
            ],
            ephemeral: true
        });
    }
}
