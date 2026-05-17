import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";

const getUserProfile = async (userId) => {
  const user = await prisma.user.findUnique({
    where: { userId, active: true },
    select: {
      userId: true,
      fullName: true,
      biography: true,
      profilePicture: true,
      email: true,
      role: true,
      registrationDate: true,
      _count: {
        select: {
          friendsAsUser1: true,
          friendsAsUser2: true,
        },
      },
    },
  });

  if (!user) {
    throw new ApiError(404, "Usuario no encontrado");
  }

  const friendsCount = user._count.friendsAsUser1 + user._count.friendsAsUser2;
  delete user._count;
  user.friendsCount = friendsCount;

  return user;
};

const updateUserProfile = async (userId, updateData) => {
  const data = {};
  if (updateData.fullName !== undefined) data.fullName = updateData.fullName;
  if (updateData.biography !== undefined) data.biography = updateData.biography;
  if (updateData.profilePicture !== undefined)
    data.profilePicture = updateData.profilePicture;

  const user = await prisma.user.update({
    where: { userId },
    data,
    select: {
      userId: true,
      fullName: true,
      biography: true,
      profilePicture: true,
    },
  });

  return user;
};

const getUserPosts = async (userId, page = 1, limit = 20) => {
  const skip = (page - 1) * limit;

  const posts = await prisma.post.findMany({
    where: { userId, active: true },
    include: {
      user: {
        select: {
          userId: true,
          fullName: true,
          profilePicture: true,
          email: true,
        },
      },
      _count: {
        select: {
          likes: true,
          comments: { where: { active: true } },
        },
      },
    },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });

  return posts.map((post) => ({
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
    commentsCount: post._count.comments,
  }));
};

const searchUsers = async (query, limit = 20) => {
  if (!query) return [];

  const users = await prisma.user.findMany({
    where: {
      fullName: {
        contains: query,
      },
      active: true,
    },
    select: {
      userId: true,
      fullName: true,
      profilePicture: true,
      biography: true,
    },
    take: limit,
  });

  return users;
};

export default {
  getUserProfile,
  updateUserProfile,
  getUserPosts,
  searchUsers,
};
