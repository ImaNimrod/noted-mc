const createError = require("http-errors");
const express = require("express");
const mongoose = require("mongoose");

const router = express.Router();

const Song = require("../models/songModel.js");

let queue = [];
let currentSongId = null;

router.get("/", (req, res, next) => {
    if (queue.length === 0) {
        res.status(204).json({});
    } else {
        res.status(200).json({queue});
    }
});

router.get("/next", (req, res, next) => {
    if (queue.length === 0) {
        res.status(204).json({});
    } else {
        currentSongId = queue.pop();

        res.status(200).json({
            "_id": `${currentSongId}`
        });
    }
});

router.get("/now", (req, res, next) => {
    if (currentSongId !== null) {
        res.status(200).json({
            "_id": `${currentSongId}`
        });
    } else {
        res.status(204).json({});
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
                queue.push(songId);

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
