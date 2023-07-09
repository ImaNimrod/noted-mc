const mongoose = require("mongoose");

let songSchema = new mongoose.Schema({
	name: { type: String, required: true },
    datePosted: { type: Date, required: true },

    file: { 
        data: { type: Buffer, required: true },
        contentType: { type: String, required: true },
    }
});

module.exports = mongoose.model("Song", songSchema, "songs");
