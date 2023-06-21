const { SlashCommandBuilder } = require("discord.js");
const axios = require("axios");

module.exports = {
    data: new SlashCommandBuilder()
    .setName("now")
    .setDescription("Displays the currently playing song"),

    async execute(interaction, client) {
        const songData = await axios.get(`${process.env.NOTED_API_URL}/songs`);
        const currentSong = await axios.get(`${process.env.NOTED_API_URL}/queue/now`);

        let currentSongName;

        songData.data.songs.forEach((song) => {
            if (currentSong.data._id === song._id) {
                currentSongName = `${song.name}`;
            }
        });

        if (currentSongName === null) {
            currentSongName = " ";
        }

        await interaction.reply({
            content: `Now playing: ${currentSongName}`,
            ephemeral: true
        });
    }
}
