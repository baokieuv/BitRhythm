import 'server-only';

import { cookies } from 'next/headers';

export type ApiEnvelope<T = unknown> = {
  success: boolean;
  code?: string;
  message?: string;
  data?: T;
};

export const ACCESS_TOKEN_COOKIE = 'beats-access-token';
export const REFRESH_TOKEN_COOKIE = 'beats-refresh-token';

export function getBackendUrl() {
  return (process.env.BACKEND_URL ?? 'http://localhost:8080').replace(/\/$/, '');
}

export async function backendFetch<T>(
  path: string,
  init: RequestInit = {},
  options: { auth?: boolean } = {},
) {
  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (options.auth !== false) {
    const store = await cookies();
    const accessToken = store.get(ACCESS_TOKEN_COOKIE)?.value;
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
  }

  const response = await fetch(`${getBackendUrl()}${path}`, {
    ...init,
    cache: 'no-store',
    headers,
  });

  const text = await response.text();
  let body: ApiEnvelope<T> | T | null = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
  }

  if (!response.ok) {
    const envelope = body as ApiEnvelope<T> | null;
    throw new Error(envelope?.message ?? `Backend request failed (${response.status})`);
  }

  if (body && typeof body === 'object' && 'success' in body) {
    return (body as ApiEnvelope<T>).data as T;
  }
  return body as T;
}

export function unwrapList<T>(value: unknown): T[] {
  if (Array.isArray(value)) return value as T[];
  if (value && typeof value === 'object') {
    const candidate = value as Record<string, unknown>;
    for (const key of ['items', 'content', 'tracks', 'genres', 'results', 'data']) {
      if (Array.isArray(candidate[key])) return candidate[key] as T[];
    }
  }
  return [];
}
