import prisma from '../utils/prisma.js';
import ApiError from '../utils/ApiError.js';

const getRepliesByComment = async (commentId) => {
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
  });

  return reply;
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

export default {
  getRepliesByComment,
  createReply,
  deleteReply,
};
