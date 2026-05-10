import express from 'express';
import commentController from '../controllers/commentController.js';
import authenticate from '../middlewares/auth.js';

const router = express.Router({ mergeParams: true });

router.use(authenticate);

// These routes are mounted at /api/posts/:postId/comments
router.get('/', commentController.getComments);
router.post('/', commentController.createComment);

// These routes are mounted at /api/comments/:id
router.delete('/:id', commentController.deleteComment);
router.post('/:id/like', commentController.toggleLike);
router.post('/:id/hide', commentController.hideComment);

export default router;
