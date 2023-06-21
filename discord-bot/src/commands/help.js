const { SlashCommandBuilder } = require("discord.js");

module.exports = {
    data: new SlashCommandBuilder()
    .setName("help")
    .setDescription("Learn how to use the bot"),

    async execute(interaction, client) {
        const serverOwner = await interaction.guild.members.fetch(interaction.guild.ownerId)

        await interaction.reply({
            content: `**About: ** Noted Bot serves as a frontend interface for users to control an ingame bot that plays songs using noteblocks. How you interact with Noted Bot will be described below. \n\n **Commands: ** you can interact with Noted Bot using 5 commands: /list, /add, /help (that's the one you are using right now), /now, and /queue. \n\n **/list: ** displays a list of the songs that Noted Bot can play. \n **/add: ** adds a song to the song queue. The name of the song is case-sensitive and it should match a name from /list. \n **/help: ** displays helpful information about how to use Noted Bot. \n **/now: ** displays the name of the song that is currently playing. \n **/queue: ** displays the songs that will be played next. Higher numbers will be played sooner. \n\n **Further information: ** if  you have any further questions or concerns, use your critical-thinking skills first and then ask ${serverOwner}`,
            ephemeral: true
        });
    }
}
