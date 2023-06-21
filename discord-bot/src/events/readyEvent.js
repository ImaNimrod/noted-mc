module.exports = {
    name: 'ready',
    once: true,

    async execute(client) {
        client.user.setStatus("online");
        console.log(`${client.user.tag} is online`);
    }
}
