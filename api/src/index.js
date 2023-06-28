const createError = require("http-errors");
const express = require("express"); 
const mongoose = require("mongoose");

require("dotenv").config();

const app = express();
const port = process.env.PORT || 3000;

const queueRoute = require("./routes/queue.js");
const songsRoute = require("./routes/songs.js");

mongoose.connect(process.env.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
});

app.use(express.json());

app.use("/songs", songsRoute);
app.use("/queue", queueRoute);

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
    console.log("noted-mc api instance started on port: " + port);
});
