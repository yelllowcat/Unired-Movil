import dotenv from 'dotenv';
dotenv.config();

const PORT = process.env.PORT || 3000;
const DATABASE_URL = process.env.DATABASE_URL;
const JWT_SECRET = process.env.JWT_SECRET || 'fallback_secret_for_development_only';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '7d';

export default {
  PORT,
  DATABASE_URL,
  JWT_SECRET,
  JWT_EXPIRES_IN,
};
