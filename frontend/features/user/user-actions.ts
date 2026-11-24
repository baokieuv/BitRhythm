'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { z } from 'zod';
import { backendFetch, ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from '@/lib/backend';

const signInSchema = z.object({
  identifier: z.string().trim().min(1, 'Username or email is required').max(254),
  password: z.string().min(1, 'Password is required').max(128),
});

export async function signIn(formData: FormData) {
  const parsed = signInSchema.safeParse({
    identifier: formData.get('identifier'),
    password: formData.get('password'),
  });
  if (!parsed.success) {
    return { error: parsed.error.issues[0].message, ok: false as const };
  }

  let tokens: {
    accessToken: string;
    refreshToken: string;
  };
  try {
    tokens = await backendFetch(
      '/api/auth/login',
      {
        body: JSON.stringify({
          identifier: parsed.data.identifier,
          password: parsed.data.password,
        }),
        method: 'POST',
      },
      { auth: false },
    );
  } catch (error) {
    return {
      error: error instanceof Error ? error.message : 'Could not sign you in. Please try again.',
      ok: false as const,
    };
  }

  const store = await cookies();
  store.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, {
    httpOnly: true,
    maxAge: 60 * 60 * 24 * 30,
    path: '/',
    sameSite: 'lax', // 30 days
  });
  store.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, {
    httpOnly: true,
    maxAge: 60 * 60 * 24 * 30,
    path: '/',
    sameSite: 'lax',
  });
  redirect('/');
}

export async function signOut() {
  const store = await cookies();
  store.delete(ACCESS_TOKEN_COOKIE);
  store.delete(REFRESH_TOKEN_COOKIE);
  redirect('/login');
}
