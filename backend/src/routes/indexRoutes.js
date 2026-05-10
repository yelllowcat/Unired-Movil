import express from 'express';
const router = express.Router();

import authRoutes from './authRoutes.js';
import userRoutes from './userRoutes.js';
import postRoutes from './postRoutes.js';
import commentRoutes from './commentRoutes.js';
import replyRoutes from './replyRoutes.js';
import friendRoutes from './friendRoutes.js';

router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/posts', postRoutes);
router.use('/posts/:postId/comments', commentRoutes);
router.use('/comments', commentRoutes);
router.use('/replies', replyRoutes);
router.use('/friends', friendRoutes);

export default router;