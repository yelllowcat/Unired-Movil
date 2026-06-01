import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";

const getUserProfile = async (userId, currentUserId) => {
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

  // Fetch posts count
  const postsCount = await prisma.post.count({
    where: { userId, active: true }
  });
  user.postsCount = postsCount;

  // Fetch likes count
  const posts = await prisma.post.findMany({
    where: { userId, active: true },
    select: {
      _count: {
        select: { likes: true }
      }
    }
  });
  const likesCount = posts.reduce((sum, p) => sum + p._count.likes, 0);
  user.likesCount = likesCount;

  // Determine friendship status
  if (userId === currentUserId) {
    user.friendshipStatus = "me";
    user.friendRequestId = null;
  } else {
    const friendship = await prisma.friend.findFirst({
      where: {
        OR: [
          { userId1: currentUserId, userId2: userId },
          { userId1: userId, userId2: currentUserId }
        ]
      }
    });

    if (friendship) {
      user.friendshipStatus = "friends";
      user.friendRequestId = null;
    } else {
      const request = await prisma.friendRequest.findFirst({
        where: {
          OR: [
            { senderId: currentUserId, receiverId: userId, status: "pending" },
            { senderId: userId, receiverId: currentUserId, status: "pending" }
          ]
        }
      });

      if (request) {
        user.friendshipStatus = request.senderId === currentUserId ? "request_sent" : "request_received";
        user.friendRequestId = request.requestId;
      } else {
        user.friendshipStatus = "none";
        user.friendRequestId = null;
      }
    }
  }

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

const searchUsers = async (query, currentUserId, limit = 20) => {
  let users;
  const currentUserIdInt = currentUserId ? parseInt(currentUserId, 10) : null;

  if (!query) {
    const friendIds = [];
    const pendingUserIds = [];
    if (currentUserIdInt) {
      // Find all friends
      const friends = await prisma.friend.findMany({
        where: {
          OR: [
            { userId1: currentUserIdInt },
            { userId2: currentUserIdInt }
          ]
        }
      });
      friends.forEach(f => {
        friendIds.push(f.userId1 === currentUserIdInt ? f.userId2 : f.userId1);
      });

      // Find pending requests
      const pendingRequests = await prisma.friendRequest.findMany({
        where: {
          status: "pending",
          OR: [
            { senderId: currentUserIdInt },
            { receiverId: currentUserIdInt }
          ]
        }
      });
      pendingRequests.forEach(r => {
        pendingUserIds.push(r.senderId === currentUserIdInt ? r.receiverId : r.senderId);
      });
    }

    const excludedIds = [currentUserIdInt, ...friendIds, ...pendingUserIds].filter(id => id !== null && id !== undefined);

    users = await prisma.user.findMany({
      where: {
        active: true,
        userId: {
          notIn: excludedIds
        }
      },
      select: {
        userId: true,
        fullName: true,
        profilePicture: true,
        biography: true,
        registrationDate: true,
      },
      take: limit,
    });
    users.sort(() => Math.random() - 0.5);
  } else {
    users = await prisma.user.findMany({
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
        registrationDate: true,
      },
      take: limit,
    });
  }

  if (!currentUserId) {
    return users.map(u => ({
      ...u,
      friendshipStatus: "none",
      friendRequestId: null
    }));
  }

  const result = [];

  for (const user of users) {
    if (user.userId === currentUserIdInt) {
      user.friendshipStatus = "me";
      user.friendRequestId = null;
    } else {
      const friendship = await prisma.friend.findFirst({
        where: {
          OR: [
            { userId1: currentUserIdInt, userId2: user.userId },
            { userId1: user.userId, userId2: currentUserIdInt }
          ]
        }
      });

      if (friendship) {
        user.friendshipStatus = "friends";
        user.friendRequestId = null;
      } else {
        const request = await prisma.friendRequest.findFirst({
          where: {
            OR: [
              { senderId: currentUserIdInt, receiverId: user.userId, status: "pending" },
              { senderId: user.userId, receiverId: currentUserIdInt, status: "pending" }
            ]
          }
        });

        if (request) {
          user.friendshipStatus = request.senderId === currentUserIdInt ? "request_sent" : "request_received";
          user.friendRequestId = request.requestId;
        } else {
          user.friendshipStatus = "none";
          user.friendRequestId = null;
        }
      }
    }
    result.push(user);
  }

  return result;
};

export default {
  getUserProfile,
  updateUserProfile,
  getUserPosts,
  searchUsers,
};
