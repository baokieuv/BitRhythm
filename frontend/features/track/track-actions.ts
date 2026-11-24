'use server';

import { revalidateTag, updateTag } from 'next/cache';
import { z } from 'zod';
import { isSlowEnabled } from '@/components/demo/demo-slow';
import { verifyAuth } from '@/features/user/user-queries';
import { backendFetch } from '@/lib/backend';
import { delay } from '@/lib/utils';

const trackIdSchema = z.string().min(1);

export async function toggleFavorite(trackId: string) {
  const userId = await verifyAuth();
  await delay(200, await isSlowEnabled());
  const id = trackIdSchema.parse(trackId);

  const favorites = await backendFetch<Array<{ id: number }>>('/api/contents/library/tracks');
  const isFavorite = favorites.some(track => String(track.id) === id);
  await backendFetch(
    `/api/contents/tracks/${id}/like`,
    { method: isFavorite ? 'DELETE' : 'PUT' },
  );

  updateTag(`track-${id}:${userId}`);
  updateTag(`favorites:${userId}`);
  revalidateTag(`discover:${userId}`, 'max');
  revalidateTag(`recommendations:${userId}`, 'max');
  return { ok: true as const };
}
