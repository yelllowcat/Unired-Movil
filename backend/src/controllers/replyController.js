import replyService from '../services/replyService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const getReplies = asyncHandler(async (req, res) => {
  const commentId = parseInt(req.params.commentId, 10);
  if (isNaN(commentId)) throw new ApiError(400, 'ID de comentario inválido');

  const replies = await replyService.getRepliesByComment(commentId, req.user.userId);
  res.status(200).json({ success: true, data: replies });
});

const createReply = asyncHandler(async (req, res) => {
  const commentId = parseInt(req.params.commentId, 10);
  if (isNaN(commentId)) throw new ApiError(400, 'ID de comentario inválido');

  const { content } = req.body;
  if (!content) throw new ApiError(400, 'El contenido no puede estar vacío');

  const reply = await replyService.createReply(commentId, req.user.userId, content);
  res.status(201).json({ success: true, data: reply });
});

const deleteReply = asyncHandler(async (req, res) => {
  const replyId = parseInt(req.params.id, 10);
  if (isNaN(replyId)) throw new ApiError(400, 'ID de respuesta inválido');

  await replyService.deleteReply(replyId, req.user.userId);
  res.status(200).json({ success: true, message: 'Respuesta eliminada' });
});

const toggleReplyLike = asyncHandler(async (req, res) => {
  const replyId = parseInt(req.params.id, 10);
  if (isNaN(replyId)) throw new ApiError(400, 'ID de respuesta inválido');

  const result = await replyService.toggleReplyLike(replyId, req.user.userId);
  res.status(200).json({ success: true, data: result });
});

export default {
  getReplies,
  createReply,
  deleteReply,
  toggleReplyLike,
};
