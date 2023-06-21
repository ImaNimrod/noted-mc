const createError = require("http-errors");
const express = require("express"); 
const mongoose = require("mongoose");

const app = express();
const port = process.env.PORT || 3000;

const queueRoute = require("./routes/queue.js");
const songsRoute = require("./routes/songs.js");

require("dotenv").config();

mongoose.connect(
    `mongodb+srv://${process.env.MONGODB_USER}:${process.env.MONGODB_PASSWORD}@${process.env.MONGODB_CLUSTER}.mongodb.net/${process.env.MONGODB_DB}?retryWrites=true&w=majority`, {
        useNewUrlParser: true,
        useUnifiedTopology: true
    }
);

app.use(express.json());

app.use("/queue", queueRoute);
app.use("/songs", songsRoute);

app.use((req, res, next) => {
    next(createError(404, "Not found"));
});

app.use((err, req, res, next) => {
    res.status(err.status || 500);
    res.send({
        error: {
            status: err.status || 500,
            message: err.message
        }
    });
});

app.listen(port, () => {
    console.log("noted-mc server started at: http://localhost:" + port);
});
