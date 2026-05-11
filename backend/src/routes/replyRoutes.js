import express from "express";
import replyController from "../controllers/replyController.js";
import authenticate from "../middlewares/auth.js";

const router = express.Router({ mergeParams: true });

router.use(authenticate);

router.get("/", replyController.getReplies);
router.post("/", replyController.createReply);

router.delete("/:id", replyController.deleteReply);

export default router;
