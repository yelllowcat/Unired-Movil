import express from 'express';
import friendController from '../controllers/friendController.js';
import authenticate from '../middlewares/auth.js';

const router = express.Router();

router.use(authenticate);

router.post('/request', friendController.sendRequest);
router.put('/request/:id', friendController.respondToRequest);
router.get('/', friendController.getFriends);
router.get('/requests/pending', friendController.getPendingRequests);
router.delete('/:id', friendController.removeFriend);

export default router;
