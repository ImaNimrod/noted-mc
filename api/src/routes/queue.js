const createError = require("http-errors");
const express = require("express");
const mongoose = require("mongoose");

const router = express.Router();

const Song = require("../models/songModel.js");

let songQueue = [];

router.get("/", (req, res, next) => {
    if (songQueue.length === 0) {
        res.status(204).json({});
    } else {
        res.status(200).json({songQueue});
    }
});

router.get("/next", (req, res, next) => {
    if (songQueue.length === 0) {
        res.status(204).json({});
    } else {
        const nextSongId = songQueue.pop();

        res.status(200).json({
            "_id": `${nextSongId}`
        });
    }
});

router.post("/:id", (req, res, next) => {
    const songId = req.params.id;

    if (!mongoose.Types.ObjectId.isValid(songId)){
        next(createError("invalid song id value"));
        return;
    }

    Song.findById(songId)
        .then((song) => {
            if (song !== null) {
                songQueue.push(songId);

                res.status(201).json({
                    "message": `added song ${songId} to queue`
                });
            } else {
                next(createError(404, "song not found"));
            }
        })
        .catch((err) => {
            next(createError(err.message));
        });
});

module.exports = router;
