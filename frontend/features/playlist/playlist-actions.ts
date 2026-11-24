'use server';

import { updateTag } from 'next/cache';
import { z } from 'zod';
import { isSlowEnabled } from '@/components/demo/demo-slow';
import { SEED_PLAYLIST_IDS } from '@/features/playlist/playlist-constants';
import { verifyAuth } from '@/features/user/user-queries';
import { backendFetch } from '@/lib/backend';
import { moderateText } from '@/lib/moderation';
import { delay } from '@/lib/utils';

const createPlaylistSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(100),
});
const idSchema = z.string().min(1);

export async function createPlaylist(formData: FormData) {
  const userId = await verifyAuth();
  await delay(300, await isSlowEnabled());
  const parsed = createPlaylistSchema.safeParse({ name: formData.get('name') });
  if (!parsed.success) {
    return { error: parsed.error.issues[0].message, ok: false as const };
  }
  const flagged = await moderateText(parsed.data.name);
  if (flagged) return { error: flagged, ok: false as const };
  const playlist = await backendFetch<{ id: number | string; name: string }>(
    '/api/contents/playlists',
    {
      body: JSON.stringify({ description: '', name: parsed.data.name }),
      method: 'POST',
    },
  );
  updateTag(`playlists:${userId}`);
  return { ok: true as const, playlist: { ...playlist, id: String(playlist.id) } };
}

export async function addToPlaylist(playlistId: string, trackId: string) {
  const userId = await verifyAuth();
  await delay(200, await isSlowEnabled());
  const parsedPlaylistId = idSchema.parse(playlistId);
  const parsedTrackId = idSchema.parse(trackId);
  if (SEED_PLAYLIST_IDS.has(parsedPlaylistId)) {
    return { error: "Can't modify a demo playlist", ok: false as const };
  }
  await backendFetch(`/api/contents/playlists/${parsedPlaylistId}/tracks`, {
    body: JSON.stringify({ trackId: Number(parsedTrackId) }),
    method: 'POST',
  });
  updateTag(`playlist-${parsedPlaylistId}`);
  updateTag(`playlists:${userId}`);
  return { ok: true as const };
}

export async function removeFromPlaylist(playlistId: string, trackId: string) {
  const userId = await verifyAuth();
  await delay(200, await isSlowEnabled());
  const parsedPlaylistId = idSchema.parse(playlistId);
  const parsedTrackId = idSchema.parse(trackId);
  if (SEED_PLAYLIST_IDS.has(parsedPlaylistId)) {
    return { error: "Can't modify a demo playlist", ok: false as const };
  }
  await backendFetch(`/api/contents/playlists/${parsedPlaylistId}/tracks/${parsedTrackId}`, {
    method: 'DELETE',
  });
  updateTag(`playlist-${parsedPlaylistId}`);
  updateTag(`playlists:${userId}`);
  return { ok: true as const };
}

export async function deletePlaylist(playlistId: string) {
  const userId = await verifyAuth();
  const id = idSchema.parse(playlistId);
  if (SEED_PLAYLIST_IDS.has(id)) return { error: "Can't delete a demo playlist", ok: false as const };
  await delay(300, await isSlowEnabled());
  await backendFetch(`/api/contents/playlists/${id}`, { method: 'DELETE' });
  updateTag(`playlists:${userId}`);
  updateTag(`playlist-${id}`);
  return { ok: true as const };
}
