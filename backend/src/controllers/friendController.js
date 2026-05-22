import friendService from '../services/friendService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const sendRequest = asyncHandler(async (req, res) => {
  const { receiverId } = req.body;
  if (!receiverId) throw new ApiError(400, 'Falta el ID del receptor');

  const request = await friendService.sendFriendRequest(req.user.userId, parseInt(receiverId, 10));
  res.status(201).json({ success: true, data: request });
});

const respondToRequest = asyncHandler(async (req, res) => {
  const requestId = parseInt(req.params.id, 10);
  const { status } = req.body;

  if (isNaN(requestId)) throw new ApiError(400, 'ID de solicitud inválido');
  if (!status) throw new ApiError(400, 'Falta el estado');

  const result = await friendService.respondToRequest(requestId, req.user.userId, status);
  res.status(200).json({ success: true, data: result });
});

const getFriends = asyncHandler(async (req, res) => {
  const friends = await friendService.getFriends(req.user.userId);
  res.status(200).json({ success: true, data: friends });
});

const getPendingRequests = asyncHandler(async (req, res) => {
  const requests = await friendService.getPendingRequests(req.user.userId);
  res.status(200).json({ success: true, data: requests });
});

const removeFriend = asyncHandler(async (req, res) => {
  const friendId = parseInt(req.params.id, 10);
  if (isNaN(friendId)) throw new ApiError(400, 'ID de amigo inválido');

  await friendService.removeFriend(req.user.userId, friendId);
  res.status(200).json({ success: true, message: 'Amigo eliminado' });
});

const getSentRequests = asyncHandler(async (req, res) => {
  const requests = await friendService.getSentRequests(req.user.userId);
  res.status(200).json({ success: true, data: requests });
});

const cancelFriendRequest = asyncHandler(async (req, res) => {
  const requestId = parseInt(req.params.id, 10);
  if (isNaN(requestId)) throw new ApiError(400, 'ID de solicitud inválido');

  await friendService.cancelFriendRequest(requestId, req.user.userId);
  res.status(200).json({ success: true, message: 'Solicitud cancelada' });
});

export default {
  sendRequest,
  respondToRequest,
  getFriends,
  getPendingRequests,
  removeFriend,
  getSentRequests,
  cancelFriendRequest,
};
