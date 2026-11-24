import 'server-only';

import { cacheLife, cacheTag } from 'next/cache';
import { isSlowEnabled } from '@/components/demo/demo-slow';
import { backendFetch, unwrapList } from '@/lib/backend';
import { delay } from '@/lib/utils';

export async function getGenres() {
  return getGenresCached(await isSlowEnabled());
}

async function getGenresCached(slow: boolean) {
  'use cache';
  cacheTag('genres');
  cacheLife('days');

  await delay(600, slow);
  const rows = await backendFetch<unknown[]>('/api/contents/genres', {}, { auth: false });
  return unwrapList<{ id: number; name: string; slug: string }>(rows).map(r => ({
    count: 0,
    genre: r.name,
  }));
}
