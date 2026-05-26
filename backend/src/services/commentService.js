import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";
import notificationService from "./notificationService.js";

const getCommentsByPost = async (postId, currentUserId, page = 1, limit = 10) => {
  const skip = (page - 1) * limit;

  const comments = await prisma.comment.findMany({
    where: { postId, active: true },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
        },
      },
      likes: {
        where: { userId: currentUserId },
        select: { likeId: true },
      },
      _count: {
        select: {
          likes: true,
          replies: { where: { active: true } },
        },
      },
    },
    orderBy: { createdAt: "asc" },
    skip,
    take: limit,
  });

  return comments.map((comment) => ({
    commentId: comment.commentId,
    postId: comment.postId,
    userId: comment.userId,
    content: comment.content,
    createdAt: comment.createdAt,
    fullName: comment.user.fullName,
    profilePicture: comment.user.profilePicture,
    likesCount: comment._count.likes,
    repliesCount: comment._count.replies,
    hasLiked: comment.likes ? comment.likes.length > 0 : false,
  }));
};

const createComment = async (postId, userId, content) => {
  const post = await prisma.post.findUnique({
    where: { postId, active: true },
  });
  if (!post) throw new ApiError(404, "Publicación no encontrada");

  const comment = await prisma.comment.create({
    data: {
      postId,
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

  await notificationService.createNotification(post.userId, userId, "comment", postId, comment.commentId);

  return {
    commentId: comment.commentId,
    postId: comment.postId,
    userId: comment.userId,
    content: comment.content,
    createdAt: comment.createdAt,
    fullName: comment.user.fullName,
    profilePicture: comment.user.profilePicture,
    likesCount: 0,
    repliesCount: 0,
    hasLiked: false,
  };
};

const deleteComment = async (commentId, userId) => {
  const comment = await prisma.comment.findUnique({
    where: { commentId, active: true },
  });

  if (!comment) throw new ApiError(404, "Comentario no encontrado");
  if (comment.userId !== userId)
    throw new ApiError(403, "No tienes permiso para eliminar este comentario");

  await prisma.comment.update({
    where: { commentId },
    data: { active: false },
  });

  return { success: true };
};

const toggleCommentLike = async (commentId, userId) => {
  const comment = await prisma.comment.findUnique({
    where: { commentId, active: true },
  });
  if (!comment) throw new ApiError(404, "Comentario no encontrado");

  const existingLike = await prisma.commentLike.findUnique({
    where: {
      commentId_userId: { commentId, userId },
    },
  });

  if (existingLike) {
    await prisma.commentLike.delete({
      where: { likeId: existingLike.likeId },
    });
    return { liked: false };
  } else {
    await prisma.commentLike.create({
      data: { commentId, userId },
    });
    await notificationService.createNotification(comment.userId, userId, "comment_like", comment.postId, commentId);
    return { liked: true };
  }
};

const hideComment = async (commentId, userId) => {
  const comment = await prisma.comment.findUnique({
    where: { commentId, active: true },
  });
  if (!comment) throw new ApiError(404, "Comentario no encontrado");

  await prisma.hiddenComment.create({
    data: {
      commentId,
      userId,
    },
  });

  return { success: true };
};

export default {
  getCommentsByPost,
  createComment,
  deleteComment,
  toggleCommentLike,
  hideComment,
};
