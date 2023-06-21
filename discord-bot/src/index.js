const { Client, IntentsBitField } = require("discord.js");
const dotenv = require("dotenv");

dotenv.config();

const client = new Client({
    intents: [
        IntentsBitField.Flags.Guilds,
        IntentsBitField.Flags.GuildMembers,
        IntentsBitField.Flags.GuildMessages,
        IntentsBitField.Flags.MessageContent,
    ]
});

client.on("ready", (c) => {
    console.log(`${c.user.tag} is online`);
});

client.on('messageCreate', (message) => {
    if (message.author.bot)
        return;

    if (message.content === "test") {
        message.reply("bruh");
    }
})

client.login(process.env.TOKEN);
