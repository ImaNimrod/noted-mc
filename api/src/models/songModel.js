const mongoose = require("mongoose");

let songSchema = new mongoose.Schema({
	name: { type: String, required: true },
    datePosted: { type: Date, required: true },

    file: { 
        data: Buffer, 
        contentType: String 
    }
});

module.exports = mongoose.model("Song", songSchema);
