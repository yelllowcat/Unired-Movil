import express from 'express';
import userController from '../controllers/userController.js';
import authenticate from '../middlewares/auth.js';
import upload from '../middlewares/upload.js';

const router = express.Router();

// Require auth for all user routes
router.use(authenticate);

router.get('/search', userController.search);
router.get('/:id', userController.getProfile);
router.put('/:id', upload.single('profilePicture'), userController.updateProfile);
router.get('/:id/posts', userController.getPosts);

export default router;
