import jwt from 'jsonwebtoken';
import ApiError from '../utils/ApiError.js';
import env from '../config/env.js';

const authenticate = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(new ApiError(401, 'Please authenticate'));
  }

  const token = authHeader.split(' ')[1];

  try {
    const payload = jwt.verify(token, env.JWT_SECRET);
    req.user = payload;
    next();
  } catch (error) {
    return next(new ApiError(401, 'Invalid or expired token'));
  }
};

export default authenticate;
