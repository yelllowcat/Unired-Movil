import express from 'express';
import replyController from '../controllers/replyController.js';
import authenticate from '../middlewares/auth.js';

const router = express.Router({ mergeParams: true });

router.use(authenticate);

// These routes are mounted at /api/comments/:commentId/replies
router.get('/', replyController.getReplies);
router.post('/', replyController.createReply);

// These routes are mounted at /api/replies/:id
router.delete('/:id', replyController.deleteReply);

export default router;
