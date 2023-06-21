const express = require("express"); 

const app        = express();
const port       = process.env.PORT || 3000;

const queueRoute = require("./routes/queue.js");

app.use(express.json());

app.get("/", (req, res) => {
    res.status(200).send("<h1>noted-mc server interface</h1>");
})

app.use("/queue", queueRoute);

app.listen(port, () => {
    console.log("noted-mc server started at: http://localhost:" + port);
});
