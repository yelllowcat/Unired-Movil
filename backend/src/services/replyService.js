import prisma from '../utils/prisma.js';
import ApiError from '../utils/ApiError.js';

const getRepliesByComment = async (commentId, currentUserId) => {
  const replies = await prisma.reply.findMany({
    where: { commentId, active: true },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
        },
      },
      reply_likes: {
        where: { user_id: currentUserId || -1 },
        select: { like_id: true },
      },
      _count: {
        select: {
          reply_likes: true,
        },
      },
    },
    orderBy: { createdAt: 'asc' },
  });

  return replies.map(reply => ({
    replyId: reply.replyId,
    commentId: reply.commentId,
    userId: reply.userId,
    content: reply.content,
    createdAt: reply.createdAt,
    fullName: reply.user.fullName,
    profilePicture: reply.user.profilePicture,
    likesCount: reply._count.reply_likes,
    hasLiked: reply.reply_likes ? reply.reply_likes.length > 0 : false,
  }));
};

const createReply = async (commentId, userId, content) => {
  const comment = await prisma.comment.findUnique({ where: { commentId, active: true } });
  if (!comment) throw new ApiError(404, 'Comentario no encontrado');

  const reply = await prisma.reply.create({
    data: {
      commentId,
      userId,
      content,
    },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
        },
      },
    },
  });

  return {
    replyId: reply.replyId,
    commentId: reply.commentId,
    userId: reply.userId,
    content: reply.content,
    createdAt: reply.createdAt,
    fullName: reply.user.fullName,
    profilePicture: reply.user.profilePicture,
    likesCount: 0,
    hasLiked: false,
  };
};

const deleteReply = async (replyId, userId) => {
  const reply = await prisma.reply.findUnique({ where: { replyId, active: true } });
  
  if (!reply) throw new ApiError(404, 'Respuesta no encontrada');
  if (reply.userId !== userId) throw new ApiError(403, 'No tienes permiso para eliminar esta respuesta');

  await prisma.reply.update({
    where: { replyId },
    data: { active: false },
  });

  return { success: true };
};

const toggleReplyLike = async (replyId, userId) => {
  const reply = await prisma.reply.findUnique({
    where: { replyId, active: true },
  });
  if (!reply) throw new ApiError(404, "Respuesta no encontrada");

  const existingLike = await prisma.reply_likes.findUnique({
    where: {
      reply_id_user_id: {
        reply_id: replyId,
        user_id: userId,
      },
    },
  });

  if (existingLike) {
    await prisma.reply_likes.delete({
      where: { like_id: existingLike.like_id },
    });
    return { liked: false };
  } else {
    await prisma.reply_likes.create({
      data: {
        reply_id: replyId,
        user_id: userId,
      },
    });
    return { liked: true };
  }
};

export default {
  getRepliesByComment,
  createReply,
  deleteReply,
  toggleReplyLike,
};
