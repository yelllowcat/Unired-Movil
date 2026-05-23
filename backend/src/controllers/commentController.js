import commentService from '../services/commentService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const getComments = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.postId, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const comments = await commentService.getCommentsByPost(postId, req.user.userId);
  res.status(200).json({ success: true, data: comments });
});

const createComment = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.postId, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const { content } = req.body;
  if (!content) throw new ApiError(400, 'El contenido no puede estar vacío');

  const comment = await commentService.createComment(postId, req.user.userId, content);
  res.status(201).json({ success: true, data: comment });
});

const deleteComment = asyncHandler(async (req, res) => {
  const commentId = parseInt(req.params.id, 10);
  if (isNaN(commentId)) throw new ApiError(400, 'ID de comentario inválido');

  await commentService.deleteComment(commentId, req.user.userId);
  res.status(200).json({ success: true, message: 'Comentario eliminado' });
});

const toggleLike = asyncHandler(async (req, res) => {
  const commentId = parseInt(req.params.id, 10);
  if (isNaN(commentId)) throw new ApiError(400, 'ID de comentario inválido');

  const result = await commentService.toggleCommentLike(commentId, req.user.userId);
  res.status(200).json({ success: true, data: result });
});

const hideComment = asyncHandler(async (req, res) => {
  const commentId = parseInt(req.params.id, 10);
  if (isNaN(commentId)) throw new ApiError(400, 'ID de comentario inválido');

  await commentService.hideComment(commentId, req.user.userId);
  res.status(200).json({ success: true, message: 'Comentario ocultado' });
});

export default {
  getComments,
  createComment,
  deleteComment,
  toggleLike,
  hideComment,
};
