import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";

const createNotification = async (userId, senderId, type, postId = null, commentId = null, replyId = null) => {
  // If the action is triggered by the user on their own content, don't notify them
  if (userId === senderId) return null;

  return await prisma.notification.create({
    data: {
      userId,
      senderId,
      type,
      postId,
      commentId,
      replyId,
    },
  });
};

const getNotifications = async (userId) => {
  const notifications = await prisma.notification.findMany({
    where: { userId },
    include: {
      sender: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
        },
      },
    },
    orderBy: { createdAt: "desc" },
  });

  return notifications.map((notification) => ({
    notificationId: notification.notificationId,
    userId: notification.userId,
    senderId: notification.senderId,
    senderName: notification.sender.fullName,
    senderPicture: notification.sender.profilePicture,
    type: notification.type,
    postId: notification.postId,
    commentId: notification.commentId,
    replyId: notification.replyId,
    isRead: notification.isRead,
    createdAt: notification.createdAt,
  }));
};

const markAsRead = async (notificationId, userId) => {
  const notification = await prisma.notification.findUnique({
    where: { notificationId },
  });

  if (!notification) throw new ApiError(404, "Notificación no encontrada");
  if (notification.userId !== userId) {
    throw new ApiError(403, "No tienes permiso para modificar esta notificación");
  }

  return await prisma.notification.update({
    where: { notificationId },
    data: { isRead: true },
  });
};

const markAllAsRead = async (userId) => {
  await prisma.notification.updateMany({
    where: { userId, isRead: false },
    data: { isRead: true },
  });
  return { success: true };
};

const getUnreadCount = async (userId) => {
  const count = await prisma.notification.count({
    where: { userId, isRead: false },
  });
  return { unreadCount: count };
};

export default {
  createNotification,
  getNotifications,
  markAsRead,
  markAllAsRead,
  getUnreadCount,
};
