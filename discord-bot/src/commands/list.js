const { Colors, EmbedBuilder, SlashCommandBuilder } = require("discord.js");
const axios = require("axios");

require("dotenv").config();

module.exports = {
    data: new SlashCommandBuilder()
    .setName("list")
    .setDescription("List songs that can be played"),

    async execute(interaction, client) {
        const res = await axios.get(`${process.env.NOTED_API_URL}/songs`);

        let songList = [];
        res.data.songs.forEach((song) => {
            songList.push(song.name);
        });

        if (songList.length === 0) {
            return await interaction.reply({
                content: "No songs to list",
                ephemeral: true
            });
        }

        await interaction.reply({
            embeds: [
                new EmbedBuilder().setTitle("Song List")
                                  .setColor(Colors.Blue)
                                  .setDescription("- " + songList.join("\n- "))
                                  .setFooter({ text: "noted-mc v1.0.0" })
            ],
            ephemeral: true
        });
    }
}
