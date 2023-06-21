const { SlashCommandBuilder } = require("discord.js");
const axios = require("axios");

module.exports = {
    data: new SlashCommandBuilder()
    .setName("add")
    .setDescription("Adds a song to the song queue")
    .addStringOption(option => option.setName("song")
                                     .setDescription("The name of the song you want to add")
                                     .setRequired(true)),

    async execute(interaction, client) {
        const songName = interaction.options.getString("song");
        const res = await axios.get(`${process.env.NOTED_API_URL}/songs`);

        let songId;

        res.data.songs.forEach((song) => {
            if (song.name === songName) {
                songId = song._id;
            }
        });

        if (!songId) {
            return await interaction.reply({
                content: "Song not found",
                ephemeral: true
            });
        }

        axios.post(`${process.env.NOTED_API_URL}/queue/${songId}`);

        await client.channels.cache
                             .get("1105280211236442132")
                             .send(`Added song **${songName}** to the queue`);
    }
}
