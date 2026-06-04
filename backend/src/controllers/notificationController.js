import notificationService from '../services/notificationService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const getNotifications = asyncHandler(async (req, res) => {
  const limit = req.query.limit ? parseInt(req.query.limit, 10) : 20;
  const cursor = req.query.cursor ? parseInt(req.query.cursor, 10) : null;
  const notifications = await notificationService.getNotifications(req.user.userId, limit, cursor);
  res.status(200).json({ success: true, data: notifications });
});

const markAsRead = asyncHandler(async (req, res) => {
  const notificationId = parseInt(req.params.id, 10);
  if (isNaN(notificationId)) throw new ApiError(400, 'ID de notificación inválido');

  const result = await notificationService.markAsRead(notificationId, req.user.userId);
  res.status(200).json({ success: true, data: result });
});

const markAllAsRead = asyncHandler(async (req, res) => {
  const result = await notificationService.markAllAsRead(req.user.userId);
  res.status(200).json({ success: true, data: result });
});

const getUnreadCount = asyncHandler(async (req, res) => {
  const result = await notificationService.getUnreadCount(req.user.userId);
  res.status(200).json({ success: true, data: result });
});

export default {
  getNotifications,
  markAsRead,
  markAllAsRead,
  getUnreadCount,
};
