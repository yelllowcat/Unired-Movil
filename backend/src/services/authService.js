import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";
import env from "../config/env.js";

const registerUser = async ({ fullName, email, password }) => {
  console.log('[registerUser] checking existing user...');
  const existingUser = await prisma.user.findUnique({ where: { email } });
  console.log('[registerUser] existingUser check done:', !!existingUser);
  if (existingUser) {
    throw new ApiError(400, "El correo ya está registrado");
  }

  console.log('[registerUser] hashing password...');
  const salt = await bcrypt.genSalt(10);
  const hashedPassword = await bcrypt.hash(password, salt);

  console.log('[registerUser] creating user in DB...');
  const newUser = await prisma.user.create({
    data: {
      fullName,
      email,
      password: hashedPassword,
    },
  });
  console.log('[registerUser] user created:', newUser.userId);

  const token = jwt.sign(
    { userId: newUser.userId, role: newUser.role },
    env.JWT_SECRET,
    { expiresIn: env.JWT_EXPIRES_IN },
  );

  return {
    token,
    user: {
      userId: newUser.userId,
      fullName: newUser.fullName,
      biography: newUser.biography,
      profilePicture: newUser.profilePicture,
      email: newUser.email,
      role: newUser.role,
      registrationDate: newUser.registrationDate,
    },
  };
};

const loginUser = async ({ email, password }) => {
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user || !user.active) {
    throw new ApiError(401, "Correo o contraseña incorrectos");
  }

  const isMatch = await bcrypt.compare(password, user.password);
  if (!isMatch) {
    throw new ApiError(401, "Correo o contraseña incorrectos");
  }

  const token = jwt.sign(
    { userId: user.userId, role: user.role },
    env.JWT_SECRET,
    { expiresIn: env.JWT_EXPIRES_IN },
  );

  return {
    token,
    user: {
      userId: user.userId,
      fullName: user.fullName,
      biography: user.biography,
      profilePicture: user.profilePicture,
      email: user.email,
      role: user.role,
      registrationDate: user.registrationDate,
    },
  };
};

export default {
  registerUser,
  loginUser,
};
