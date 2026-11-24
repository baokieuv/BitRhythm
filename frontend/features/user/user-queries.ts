import 'server-only';

import { cacheLife } from 'next/cache';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { backendFetch, ACCESS_TOKEN_COOKIE } from '@/lib/backend';

export async function getCurrentUser() {
  'use cache: private';
  cacheLife({ stale: Infinity });

  const store = await cookies();
  if (!store.get(ACCESS_TOKEN_COOKIE)?.value) return '';
  try {
    const user = await backendFetch<{ id: number | string }>('/api/auth/me');
    return String(user.id);
  } catch {
    return '';
  }
}

export async function getCurrentUserName() {
  await verifyAuth();
  try {
    const user = await backendFetch<{ username?: string; name?: string }>('/api/auth/me');
    return user.name ?? user.username ?? 'listener';
  } catch {
    return 'listener';
  }
}

export async function verifyAuth() {
  const userId = await getCurrentUser();
  if (!userId) {
    redirect('/login');
  }
  return userId;
}
