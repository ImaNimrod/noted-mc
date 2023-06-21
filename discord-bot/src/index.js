const { Client, Collection, IntentsBitField } = require("discord.js");

require("dotenv").config();

const client = new Client({
    intents: [
        IntentsBitField.Flags.Guilds,
        IntentsBitField.Flags.GuildMembers,
        IntentsBitField.Flags.GuildMessages,
        IntentsBitField.Flags.MessageContent,
    ]
});

client.commands = new Collection();

(async () => {
    require("./handlers/commandHandler.js")(client);
    require("./handlers/eventHandler.js")(client);

    client.handleCommands("./src/commands");
    client.handleEvents("./src/events");

    client.login(process.env.DISCORD_TOKEN);
})();
