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

        const currentSongId = await axios.get(`${process.env.NOTED_API_URL}/queue/now`);

        let currentSongName;

        songData.data.songs.forEach((song) => {
            if (currentSongId === song._id) {
                currentSongName = `${song.name}`;
            }
        });

        let songList = [];
        let songName;
        let i = 1;

        queueData.data.queue.forEach((item) => {
            songData.data.songs.forEach((song) => {
                if (item === song._id) {
                    songName = `${i}. ${song.name}`;
                    songList.push(songName);
                }
            });

            i++;
        });

        await interaction.reply({
            embeds: [
                new EmbedBuilder().setTitle("Song Queue")
                                  .setColor(Colors.Blue)
                                  .setDescription("Now Playing: " currentSongName + "\n    " + songList.join("\n   "))
                                  .setFooter({ text: "noted-mc v1.0.0" })
            ],
            ephemeral: true
        });
    }
}
