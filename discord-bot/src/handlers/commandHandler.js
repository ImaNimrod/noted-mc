const { REST } = require("@discordjs/rest");
const { Routes } = require('discord-api-types/v9');
const fs = require("fs");

require("dotenv").config();

const clientId = process.env.DISCORD_CLIENT_ID;
const guildId = process.env.DISCORD_GUILD_ID;

module.exports = (client) => {
    client.handleCommands = async (path) => {
        client.commandArray = [];

        const commandFiles = fs.readdirSync(`${path}`).filter(file => file.endsWith(".js"));

        for (const file of commandFiles) {
            const command = require(`../commands/${file}`);

            client.commands.set(command.data.name, command);
            client.commandArray.push(command.data.toJSON());
        }

        const rest = new REST({ version: '9' }).setToken(process.env.DISCORD_TOKEN);

        (async () => {
            try {
                console.log("Started refreshing application commands");

                await rest.put(
                    Routes.applicationCommands(clientId), {
                        body: client.commandArray
                    },
                );

                console.log("Successfully reloaded application commands");
            } catch (error) {
                console.error(error);
            }
        })();
    };
};
