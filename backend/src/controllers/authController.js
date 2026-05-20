import authService from '../services/authService.js';
import asyncHandler from '../utils/asyncHandler.js';
import ApiError from '../utils/ApiError.js';

const register = asyncHandler(async (req, res) => {
  console.log('[register] body received:', req.body);
  const { fullName, email, password } = req.body;

  if (!fullName || !email || !password) {
    throw new ApiError(400, 'Por favor provea nombre, correo y contraseña');
  }

  console.log('[register] calling authService.registerUser...');
  const result = await authService.registerUser({ fullName, email, password });
  console.log('[register] done, sending 201...');
  res.status(201).json({
    success: true,
    data: result,
  });
});

const login = asyncHandler(async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    throw new ApiError(400, 'Por favor provea correo y contraseña');
  }

  const authData = await authService.loginUser({ email, password });
  res.status(200).json({
    success: true,
    data: authData,
  });
});

export default {
  register,
  login,
};
