import { PrismaClient } from '@prisma/client';
import { PrismaMariaDb } from '@prisma/adapter-mariadb';
import bcrypt from 'bcryptjs';
import dotenv from 'dotenv';

dotenv.config();

const databaseUrl = process.env.DATABASE_URL;
if (!databaseUrl) {
  console.error('Error: DATABASE_URL is not set in the environment.');
  process.exit(1);
}

const adapter = new PrismaMariaDb(databaseUrl);
const prisma = new PrismaClient({ adapter });

async function main() {
  console.log('Starting database seed...');

  // Clean existing data in dependency order
  console.log('Cleaning existing data...');
  await prisma.userUpdateLog.deleteMany({});
  await prisma.friend.deleteMany({});
  await prisma.friendRequest.deleteMany({});
  await prisma.reply.deleteMany({});
  await prisma.commentLike.deleteMany({});
  await prisma.like.deleteMany({});
  await prisma.hiddenComment.deleteMany({});
  await prisma.comment.deleteMany({});
  await prisma.post.deleteMany({});
  await prisma.user.deleteMany({});
  console.log('Data cleaned.');

  // Create password hash
  const salt = await bcrypt.genSalt(10);
  const adminPassword = await bcrypt.hash('admin123', salt);
  const barberPassword = await bcrypt.hash('barber123', salt);
  const defaultPassword = await bcrypt.hash('password123', salt);

  // 1. Create Users
  console.log('Creating users...');
  const admin = await prisma.user.create({
    data: {
      fullName: 'Admin User',
      email: 'admin@unired.com',
      password: adminPassword,
      role: 'admin',
      biography: 'System Administrator for Unired Movil.',
      profilePicture: 'default_avatar.png',
    },
  });

  const barber = await prisma.user.create({
    data: {
      fullName: 'Barber John',
      email: 'barber@unired.com',
      password: barberPassword,
      role: 'barber',
      biography: 'Expert barber with 10+ years of experience in modern haircuts.',
      profilePicture: 'default_avatar.png',
    },
  });

  const alice = await prisma.user.create({
    data: {
      fullName: 'Alice Smith',
      email: 'alice@unired.com',
      password: defaultPassword,
      role: 'user',
      biography: 'Design enthusiast and social butterfly.',
      profilePicture: 'default_avatar.png',
    },
  });

  const bob = await prisma.user.create({
    data: {
      fullName: 'Bob Johnson',
      email: 'bob@unired.com',
      password: defaultPassword,
      role: 'user',
      biography: 'Tech lover and coffee addict.',
      profilePicture: 'default_avatar.png',
    },
  });

  const charlie = await prisma.user.create({
    data: {
      fullName: 'Charlie Brown',
      email: 'charlie@unired.com',
      password: defaultPassword,
      role: 'user',
      biography: 'Always looking on the bright side of life!',
      profilePicture: 'default_avatar.png',
    },
  });

  console.log(`Created users:
  - Admin: admin@unired.com (admin123)
  - Barber: barber@unired.com (barber123)
  - Alice: alice@unired.com (password123)
  - Bob: bob@unired.com (password123)
  - Charlie: charlie@unired.com (password123)`);

  // 2. Create Friends and Friend Requests
  console.log('Creating friendship relations...');
  // Friendship between Alice and Bob
  await prisma.friend.create({
    data: {
      userId1: Math.min(alice.userId, bob.userId),
      userId2: Math.max(alice.userId, bob.userId),
    },
  });

  // Friend request from Charlie to Alice (Pending)
  await prisma.friendRequest.create({
    data: {
      senderId: charlie.userId,
      receiverId: alice.userId,
      status: 'pending',
    },
  });

  // Friend request from Bob to Charlie (Accepted, already added to Friend)
  await prisma.friendRequest.create({
    data: {
      senderId: bob.userId,
      receiverId: charlie.userId,
      status: 'accepted',
    },
  });
  await prisma.friend.create({
    data: {
      userId1: Math.min(bob.userId, charlie.userId),
      userId2: Math.max(bob.userId, charlie.userId),
    },
  });

  // 3. Create Posts
  console.log('Creating posts...');
  const post1 = await prisma.post.create({
    data: {
      userId: alice.userId,
      content: 'Just had an amazing haircut at the Unired shop! High recommended! ✂️💈',
    },
  });

  const post2 = await prisma.post.create({
    data: {
      userId: bob.userId,
      content: 'Hello world! This is my first post on Unired Movil. Excited to be here!',
    },
  });

  const post3 = await prisma.post.create({
    data: {
      userId: barber.userId,
      content: 'Check out the new styles we are doing today. Slots are open for bookings! 📅',
    },
  });

  // 4. Create Likes on Posts
  console.log('Creating post likes...');
  await prisma.like.create({
    data: {
      postId: post1.postId,
      userId: bob.userId,
    },
  });

  await prisma.like.create({
    data: {
      postId: post1.postId,
      userId: barber.userId,
    },
  });

  await prisma.like.create({
    data: {
      postId: post3.postId,
      userId: alice.userId,
    },
  });

  // 5. Create Comments
  console.log('Creating comments...');
  const comment1 = await prisma.comment.create({
    data: {
      postId: post1.postId,
      userId: bob.userId,
      content: 'Looks awesome, Alice! Which barber did it?',
    },
  });

  const comment2 = await prisma.comment.create({
    data: {
      postId: post1.postId,
      userId: barber.userId,
      content: 'Thanks for coming, Alice! It was a pleasure!',
    },
  });

  const comment3 = await prisma.comment.create({
    data: {
      postId: post3.postId,
      userId: charlie.userId,
      content: 'Just booked my slot for this afternoon!',
    },
  });

  // 6. Create Comment Likes
  console.log('Creating comment likes...');
  await prisma.commentLike.create({
    data: {
      commentId: comment1.commentId,
      userId: alice.userId,
    },
  });

  // 7. Create Replies
  console.log('Creating replies...');
  await prisma.reply.create({
    data: {
      commentId: comment1.commentId,
      userId: alice.userId,
      content: 'It was Barber John! He is incredible.',
    },
  });

  await prisma.reply.create({
    data: {
      commentId: comment1.commentId,
      userId: barber.userId,
      content: '💈 Appreciate the shoutout, Alice!',
    },
  });

  console.log('Seeding completed successfully!');
}

main()
  .catch((e) => {
    console.error('Error during seeding:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
