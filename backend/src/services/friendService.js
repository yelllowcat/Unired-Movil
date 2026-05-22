import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";

const sendFriendRequest = async (senderId, receiverId) => {
  if (senderId === receiverId) {
    throw new ApiError(400, "No puedes enviarte una solicitud a ti mismo");
  }

  const receiver = await prisma.user.findUnique({
    where: { userId: receiverId, active: true },
  });
  if (!receiver) throw new ApiError(404, "Usuario no encontrado");

  const existingFriendship = await prisma.friend.findFirst({
    where: {
      OR: [
        { userId1: senderId, userId2: receiverId },
        { userId1: receiverId, userId2: senderId },
      ],
    },
  });

  if (existingFriendship) {
    throw new ApiError(400, "Ya son amigos");
  }

  const existingRequest = await prisma.friendRequest.findFirst({
    where: {
      OR: [
        { senderId, receiverId, status: "pending" },
        { senderId: receiverId, receiverId: senderId, status: "pending" },
      ],
    },
  });

  if (existingRequest) {
    throw new ApiError(
      400,
      "Ya existe una solicitud pendiente entre estos usuarios",
    );
  }

  const request = await prisma.friendRequest.create({
    data: { senderId, receiverId },
  });

  return request;
};

const respondToRequest = async (requestId, userId, status) => {
  if (!["accepted", "rejected"].includes(status)) {
    throw new ApiError(400, "Estado inválido");
  }

  const request = await prisma.friendRequest.findUnique({
    where: { requestId },
  });

  if (!request || request.status !== "pending") {
    throw new ApiError(404, "Solicitud no encontrada o ya respondida");
  }

  if (request.receiverId !== userId) {
    throw new ApiError(403, "No tienes permiso para responder esta solicitud");
  }

  await prisma.friendRequest.update({
    where: { requestId },
    data: { status, responseDate: new Date() },
  });

  if (status === "accepted") {
    const user1 = Math.min(request.senderId, request.receiverId);
    const user2 = Math.max(request.senderId, request.receiverId);

    await prisma.friend.create({
      data: { userId1: user1, userId2: user2 },
    });
  }

  return { success: true, status };
};

const getFriends = async (userId) => {
  const friends = await prisma.friend.findMany({
    where: {
      OR: [{ userId1: userId }, { userId2: userId }],
    },
    include: {
      user1: { select: { userId: true, fullName: true, profilePicture: true } },
      user2: { select: { userId: true, fullName: true, profilePicture: true } },
    },
  });

  return friends.map((f) => {
    const isUser1 = f.userId1 === userId;
    return isUser1 ? f.user2 : f.user1;
  });
};

const getPendingRequests = async (userId) => {
  const requests = await prisma.friendRequest.findMany({
    where: { receiverId: userId, status: "pending" },
    include: {
      sender: {
        select: { userId: true, fullName: true, profilePicture: true },
      },
    },
    orderBy: { requestDate: "desc" },
  });

  return requests;
};

const removeFriend = async (userId, friendId) => {
  const friendship = await prisma.friend.findFirst({
    where: {
      OR: [
        { userId1: userId, userId2: friendId },
        { userId1: friendId, userId2: userId },
      ],
    },
  });

  if (!friendship) throw new ApiError(404, "No son amigos");

  await prisma.friend.delete({
    where: { friendshipId: friendship.friendshipId },
  });

  return { success: true };
};

const getSentRequests = async (userId) => {
  const requests = await prisma.friendRequest.findMany({
    where: { senderId: userId, status: "pending" },
    include: {
      receiver: {
        select: { userId: true, fullName: true, profilePicture: true },
      },
    },
    orderBy: { requestDate: "desc" },
  });

  return requests.map((r) => {
    const { receiver, ...rest } = r;
    return { ...rest, sender: receiver };
  });
};

const cancelFriendRequest = async (requestId, userId) => {
  const request = await prisma.friendRequest.findUnique({
    where: { requestId },
  });

  if (!request) throw new ApiError(404, "Solicitud no encontrada");

  if (request.senderId !== userId) {
    throw new ApiError(403, "No tienes permiso para cancelar esta solicitud");
  }

  if (request.status !== "pending") {
    throw new ApiError(400, "Solo se pueden cancelar solicitudes pendientes");
  }

  await prisma.friendRequest.delete({
    where: { requestId },
  });

  return { success: true };
};

export default {
  sendFriendRequest,
  respondToRequest,
  getFriends,
  getPendingRequests,
  removeFriend,
  getSentRequests,
  cancelFriendRequest,
};
