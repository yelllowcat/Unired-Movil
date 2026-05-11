import { PrismaClient } from '@prisma/client';
import env from '../config/env.js';

const prisma = new PrismaClient({
  url: env.DATABASE_URL
});

export default prisma;
