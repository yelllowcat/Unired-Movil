import express from 'express';
import postController from '../controllers/postController.js';
import authenticate from '../middlewares/auth.js';
import upload from '../middlewares/upload.js';

const router = express.Router();

router.use(authenticate);

router.get('/', postController.getFeed);
router.post('/', upload.single('image'), postController.createPost);

router.get('/:id', postController.getPost);
router.put('/:id', postController.updatePost);
router.delete('/:id', postController.deletePost);

router.post('/:id/like', postController.toggleLike);
router.get('/:id/likers', postController.getLikers);

export default router;
