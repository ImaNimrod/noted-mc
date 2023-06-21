const createError = require("http-errors");
const express = require("express");
const fs = require("fs");
const mongoose = require("mongoose");
const multer = require("multer");

const Song = require("../models/songModel.js");

const fileFilter = (req, file, cb) => {
    if (file.originalname.endsWith(".mid") || file.originalname.endsWith(".midi")) {
        cb(null, true);
    } else {
        cb(null, false);
    }
}

const router = express.Router();
const uploads = multer({
    dest: "./uploads",
    fileFilter: fileFilter,
    limits: {
        fileSize: 256 * 1024    
    }
});

function SongData(data) {
    this._id = data._id;
	this.name = data.name;
    this.datePosted = data.datePosted;
}

router.get("/", (req, res, next) => {
    Song.find({})
        .then((songs) => {
            let songDataArray = [];

            songs.forEach((song) => {
                songDataArray.push(new SongData(song));
            })

            if (songDataArray.length > 0) {
                res.status(200).json({
                    count: songDataArray.length,
                    songs: songDataArray 
                });
            } else {
                res.status(204).json({});
            }
        })
        .catch((err) => {
            next(createError(err.message));
        });
});

router.get("/:id", (req, res, next) => {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)){
        next(createError("invalid song id value"));
        return;
    }

    Song.findById(req.params.id)
        .then((song) => {
            if (song !== null) {
                res.status(200)
                   .contentType(song.file.contentType)
                   .send(song.file.data);
            } else {
                next(createError(404, "song not found"));
            }
        })
        .catch((err) => {
            next(createError(err.message));
        });
});

router.post("/", uploads.single("file"), (req, res, next) => {
    const song = new Song({
        name: req.body.name,
        datePosted: Date(),
        file: { 
            data: fs.readFileSync(req.file.path),
            contentType: req.file.mimetype
        }
    });

    song.save()
        .then((result) => {
            res.status(201).json({
                "message": `posted song ${song.id}`
            });
        })
        .catch((err) => {
            next(createError(err.message));
        });
});

router.delete("/:id", (req, res, next) => {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)){
        next(createError("invalid song id value"));
        return;
    }

    Song.findByIdAndRemove(req.params.id)
        .then((result) => {
            res.status(200).json({
                "message": `deleted song ${req.params.id}`
            });
        })
        .catch(err => {
            next(createError(err.message));
        });
});

module.exports = router;
