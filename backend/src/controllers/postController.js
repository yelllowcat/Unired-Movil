import postService from '../services/postService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const getFeed = asyncHandler(async (req, res) => {
  const page = parseInt(req.query.page, 10) || 1;
  const limit = parseInt(req.query.limit, 10) || 20;

  const feed = await postService.getFeed(req.user.userId, page, limit);
  res.status(200).json({ success: true, data: feed });
});

const getPost = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.id, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const post = await postService.getPostById(postId, req.user.userId);
  res.status(200).json({ success: true, data: post });
});

const createPost = asyncHandler(async (req, res) => {
  const { content } = req.body;
  if (!content) throw new ApiError(400, 'El contenido no puede estar vacío');

  let imagePath = null;
  if (req.file) {
    imagePath = `/uploads/${req.file.filename}`;
  }

  const post = await postService.createPost(req.user.userId, content, imagePath);
  res.status(201).json({ success: true, data: post });
});

const updatePost = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.id, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const { content } = req.body;
  if (!content) throw new ApiError(400, 'El contenido no puede estar vacío');

  const post = await postService.updatePost(postId, req.user.userId, content);
  res.status(200).json({ success: true, data: post });
});

const deletePost = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.id, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  await postService.deletePost(postId, req.user.userId);
  res.status(200).json({ success: true, message: 'Publicación eliminada' });
});

const toggleLike = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.id, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const result = await postService.toggleLike(postId, req.user.userId);
  res.status(200).json({ success: true, data: result });
});

const getLikers = asyncHandler(async (req, res) => {
  const postId = parseInt(req.params.id, 10);
  if (isNaN(postId)) throw new ApiError(400, 'ID de publicación inválido');

  const likers = await postService.getLikers(postId);
  res.status(200).json({ success: true, data: likers });
});

export default {
  getFeed,
  getPost,
  createPost,
  updatePost,
  deletePost,
  toggleLike,
  getLikers,
};
