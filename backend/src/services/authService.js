import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import prisma from "../utils/prisma.js";
import ApiError from "../utils/ApiError.js";
import env from "../config/env.js";

const registerUser = async ({ fullName, email, password }) => {
  const existingUser = await prisma.user.findUnique({ where: { email } });
  if (existingUser) {
    throw new ApiError(400, "El correo ya está registrado");
  }

  const salt = await bcrypt.genSalt(10);
  const hashedPassword = await bcrypt.hash(password, salt);

  const newUser = await prisma.user.create({
    data: {
      fullName,
      email,
      password: hashedPassword,
    },
  });

  return {
    userId: newUser.userId,
    fullName: newUser.fullName,
    email: newUser.email,
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
