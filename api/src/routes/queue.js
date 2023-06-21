const express = require("express");
const router  = express.Router();

class Queue {

    constructor() {
        this.elements = {};
        this.head = 0;
        this.tail = 0;
    }

    enqueue(element) {
        this.elements[this.tail] = element;
        this.tail++;
    }

    dequeue() {
        const element = this.elements[this.head];
        delete this.elements[this.head];
        this.head++;

        return element;
    }

    get size() {
        return this.tail - this.head;
    }

    get isEmpty() {
        return this.length === 0;
    }

}

const songQueue = new Queue();

router.get("/", (req, res) => {
    if (songQueue.isEmpty) {
       res.status(204).json({ "song": null }) 
    } else {
        let songName = songQueue.dequeue();

        res.status(200).json({
            "song": songName
        });
    }
});

router.post("/", (req, res) => {
    res.status(200).json({
        "message": "add song to queue"
    });
})

module.exports = router;
