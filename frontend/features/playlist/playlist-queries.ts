import 'server-only';

import { cacheTag } from 'next/cache';
import { notFound } from 'next/navigation';
import { isSlowEnabled } from '@/components/demo/demo-slow';
import { verifyAuth } from '@/features/user/user-queries';
import { backendFetch, unwrapList } from '@/lib/backend';
import { delay } from '@/lib/utils';
import { toBackendTrack, type BackendTrack } from '@/types/track';

type BackendPlaylist = {
  id: number | string;
  name: string;
  description?: string;
  trackCount?: number;
};

function toPlaylistSummary(row: BackendPlaylist) {
  return {
    coverColor: 'from-violet-500 to-purple-600',
    description: row.description ?? '',
    id: String(row.id),
    name: row.name,
    trackCount: row.trackCount ?? 0,
  };
}

export async function getPlaylists() {
  const userId = await verifyAuth();
  return getPlaylistsForUser(userId, await isSlowEnabled());
}

async function getPlaylistsForUser(userId: string, slow: boolean) {
  'use cache';
  cacheTag(`playlists:${userId}`);
  await delay(500, slow);
  const rows = await backendFetch<unknown>('/api/contents/me/playlists?size=100');
  return unwrapList<BackendPlaylist>(rows).map(row => ({ ...toPlaylistSummary(row), tracks: [] }));
}

export async function searchPlaylists(query: string) {
  const userId = await verifyAuth();
  return searchPlaylistsForUser(userId, query, await isSlowEnabled());
}

async function searchPlaylistsForUser(userId: string, query: string, slow: boolean) {
  'use cache';
  cacheTag(`playlists:${userId}`);
  await delay(400, slow);
  const rows = await backendFetch<unknown>('/api/contents/me/playlists?size=100');
  const needle = query.toLowerCase();
  return unwrapList<BackendPlaylist>(rows)
    .filter(row => row.name.toLowerCase().includes(needle))
    .map(row => ({ ...toPlaylistSummary(row), tracks: [] }));
}

export async function getPlaylist(id: string) {
  const userId = await verifyAuth();
  return getPlaylistForUser(id, userId, await isSlowEnabled());
}

async function getPlaylistForUser(id: string, userId: string, slow: boolean) {
  'use cache';
  cacheTag(`playlist-${id}`);
  await delay(500, slow);
  const row = await backendFetch<BackendPlaylist | null>(`/api/contents/playlists/${id}`);
  if (!row) notFound();
  const tracksData = await backendFetch<unknown>(`/api/contents/playlists/${id}/tracks?size=100`);
  const tracks = unwrapList<BackendTrack>(tracksData).map(toBackendTrack);
  return { ...toPlaylistSummary(row), trackCount: tracks.length || row.trackCount || 0, tracks };
}

export async function getPlaylistMenuItems(trackId: string) {
  const userId = await verifyAuth();
  return getPlaylistMenuItemsForUser(trackId, userId);
}

async function getPlaylistMenuItemsForUser(trackId: string, userId: string) {
  'use cache';
  cacheTag(`playlists:${userId}`);
  const rows = await backendFetch<unknown>('/api/contents/me/playlists?size=100');
  return unwrapList<BackendPlaylist>(rows).map(row => ({
    active: false,
    label: row.name,
    value: String(row.id),
  }));
}
