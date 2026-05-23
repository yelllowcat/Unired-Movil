import userService from '../services/userService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const getProfile = asyncHandler(async (req, res) => {
  const userId = parseInt(req.params.id, 10);
  if (isNaN(userId)) throw new ApiError(400, 'ID de usuario inválido');

  const user = await userService.getUserProfile(userId, req.user.userId);
  res.status(200).json({ success: true, data: user });
});

const updateProfile = asyncHandler(async (req, res) => {
  const userId = parseInt(req.params.id, 10);
  if (isNaN(userId)) throw new ApiError(400, 'ID de usuario inválido');

  // Ensure a user can only update their own profile (or admin)
  if (req.user.userId !== userId && req.user.role !== 'admin') {
    throw new ApiError(403, 'No tienes permiso para modificar este perfil');
  }

  // If a file was uploaded, set it as profilePicture
  const updateData = { ...req.body };
  if (req.file) {
    updateData.profilePicture = `/uploads/${req.file.filename}`;
  }

  const updatedUser = await userService.updateUserProfile(userId, updateData);
  res.status(200).json({ success: true, data: updatedUser });
});

const getPosts = asyncHandler(async (req, res) => {
  const userId = parseInt(req.params.id, 10);
  if (isNaN(userId)) throw new ApiError(400, 'ID de usuario inválido');

  const page = parseInt(req.query.page, 10) || 1;
  const limit = parseInt(req.query.limit, 10) || 20;

  const posts = await userService.getUserPosts(userId, page, limit);
  res.status(200).json({ success: true, data: posts });
});

const search = asyncHandler(async (req, res) => {
  const query = req.query.q;
  const users = await userService.searchUsers(query);
  res.status(200).json({ success: true, data: users });
});

export default {
  getProfile,
  updateProfile,
  getPosts,
  search,
};
