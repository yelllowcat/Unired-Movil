import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";
import notificationService from "./notificationService.js";

const getFeed = async (currentUserId, page = 1, limit = 20) => {
  const skip = (page - 1) * limit;

  const posts = await prisma.post.findMany({
    where: { active: true },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
          email: true,
        },
      },
      likes: {
        where: { userId: currentUserId }
      },
      comments: {
        where: { active: true },
        select: {
          _count: {
            select: { replies: { where: { active: true } } }
          }
        }
      },
      _count: {
        select: {
          likes: true,
        },
      },
    },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });

  return posts.map((post) => {
    const commentsCount = post.comments.length + post.comments.reduce((sum, c) => sum + c._count.replies, 0);
    return {
      postId: post.postId,
      userId: post.userId,
      content: post.content,
      image: post.image,
      createdAt: post.createdAt,
      updatedAt: post.updatedAt,
      authorName: post.user.fullName,
      authorPicture: post.user.profilePicture,
      authorEmail: post.user.email,
      likesCount: post._count.likes,
      commentsCount,
      hasLiked: post.likes.length > 0,
    };
  });
};

const getPostById = async (postId, currentUserId) => {
  const post = await prisma.post.findUnique({
    where: { postId, active: true },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
          email: true,
        },
      },
      likes: {
        where: { userId: currentUserId }
      },
      comments: {
        where: { active: true },
        select: {
          _count: {
            select: { replies: { where: { active: true } } }
          }
        }
      },
      _count: {
        select: {
          likes: true,
        },
      },
    },
  });

  if (!post) {
    throw new ApiError(404, "Publicación no encontrada");
  }

  const commentsCount = post.comments.length + post.comments.reduce((sum, c) => sum + c._count.replies, 0);

  return {
    postId: post.postId,
    userId: post.userId,
    content: post.content,
    image: post.image,
    createdAt: post.createdAt,
    updatedAt: post.updatedAt,
    authorName: post.user.fullName,
    authorPicture: post.user.profilePicture,
    authorEmail: post.user.email,
    likesCount: post._count.likes,
    commentsCount,
    hasLiked: post.likes.length > 0,
  };
};

const createPost = async (userId, content, imagePath) => {
  const post = await prisma.post.create({
    data: {
      userId,
      content,
      image: imagePath || null,
    },
  });
  return post;
};

const updatePost = async (postId, userId, content, removeImage = false) => {
  const post = await prisma.post.findUnique({
    where: { postId, active: true },
  });

  if (!post) throw new ApiError(404, "Publicación no encontrada");
  if (post.userId !== userId)
    throw new ApiError(403, "No tienes permiso para editar esta publicación");

  const updateData = { content, updatedAt: new Date() };
  if (removeImage) {
    updateData.image = null;
  }

  const updatedPost = await prisma.post.update({
    where: { postId },
    data: updateData,
  });

  return updatedPost;
};

const deletePost = async (postId, userId) => {
  const post = await prisma.post.findUnique({
    where: { postId, active: true },
  });

  if (!post) throw new ApiError(404, "Publicación no encontrada");
  if (post.userId !== userId)
    throw new ApiError(403, "No tienes permiso para eliminar esta publicación");

  await prisma.post.update({
    where: { postId },
    data: { active: false },
  });

  return { success: true };
};

const toggleLike = async (postId, userId) => {
  const post = await prisma.post.findUnique({
    where: { postId, active: true },
  });
  if (!post) throw new ApiError(404, "Publicación no encontrada");

  const existingLike = await prisma.like.findUnique({
    where: {
      postId_userId: { postId, userId },
    },
  });

  if (existingLike) {
    // Unlike
    await prisma.like.delete({
      where: { likeId: existingLike.likeId },
    });
    return { liked: false };
  } else {
    // Like
    await prisma.like.create({
      data: { postId, userId },
    });
    await notificationService.createNotification(post.userId, userId, "like", postId);
    return { liked: true };
  }
};

const getLikers = async (postId) => {
  const likes = await prisma.like.findMany({
    where: { postId },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
        },
      },
    },
    orderBy: { likedAt: "desc" },
  });

  return likes.map((like) => ({
    userId: like.user.userId,
    fullName: like.user.fullName,
    profilePicture: like.user.profilePicture,
    likedAt: like.likedAt,
  }));
};

export default {
  getFeed,
  getPostById,
  createPost,
  updatePost,
  deletePost,
  toggleLike,
  getLikers,
};
