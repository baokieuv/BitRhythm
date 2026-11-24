import 'server-only';

import { cacheLife, cacheTag } from 'next/cache';
import { notFound } from 'next/navigation';
import { isSlowEnabled } from '@/components/demo/demo-slow';
import { verifyAuth } from '@/features/user/user-queries';
import { backendFetch, unwrapList } from '@/lib/backend';
import { delay } from '@/lib/utils';
import { toBackendTrack, type BackendTrack } from '@/types/track';

const LIBRARY_PAGE_SIZE = 100;

export async function getLibrary(page: number = 1) {
  return getLibraryCached(page, await isSlowEnabled());
}

async function getLibraryCached(page: number, slow: boolean) {
  'use cache';
  cacheTag('library');
  cacheLife('hours');
  await delay(400, slow);
  const rows = await backendFetch<unknown>('/api/contents/tracks', {}, { auth: false });
  const tracks = unwrapList<BackendTrack>(rows).map(toBackendTrack);
  const start = (page - 1) * LIBRARY_PAGE_SIZE;
  const items = tracks.slice(start, start + LIBRARY_PAGE_SIZE);
  return { hasMore: tracks.length > start + LIBRARY_PAGE_SIZE, tracks: items };
}

export async function getFavorites() {
  const userId = await verifyAuth();
  return getFavoritesForUser(userId, await isSlowEnabled());
}

async function getFavoritesForUser(userId: string, slow: boolean) {
  'use cache';
  cacheTag(`favorites:${userId}`);
  await delay(500, slow);
  const rows = await backendFetch<unknown>('/api/contents/library/tracks');
  return unwrapList<BackendTrack>(rows).map(toBackendTrack);
}

export async function getUserFavoriteIds() {
  const userId = await verifyAuth();
  return getUserFavoriteIdsForUser(userId);
}

async function getUserFavoriteIdsForUser(userId: string) {
  'use cache';
  cacheTag(`favorites:${userId}`);
  const rows = await backendFetch<unknown>('/api/contents/library/tracks');
  return new Set(unwrapList<BackendTrack>(rows).map(row => String(row.id)));
}

export async function getRecentlyPlayed(limit: number = 8) {
  const userId = await verifyAuth();
  return getRecentlyPlayedForUser(userId, limit, await isSlowEnabled());
}

async function getRecentlyPlayedForUser(userId: string, limit: number, slow: boolean) {
  'use cache';
  cacheTag(`recently-played:${userId}`);
  cacheLife('minutes');
  await delay(500, slow);
  const rows = await backendFetch<unknown>(`/api/contents/library/history?size=${limit}`);
  return unwrapList<BackendTrack>(rows).map(toBackendTrack);
}

export async function getTrack(id: string) {
  const userId = await verifyAuth();
  return getTrackForUser(id, userId, await isSlowEnabled());
}

async function getTrackForUser(id: string, userId: string, slow: boolean) {
  'use cache';
  cacheTag('tracks', `track-${id}`, `track-${id}:${userId}`);
  await delay(400, slow);
  const row = await backendFetch<BackendTrack | null>(`/api/contents/tracks/${id}`);
  if (!row) notFound();
  return toBackendTrack(row);
}

export async function getMostPlayed(limit: number = 8) {
  return getMostPlayedCached(limit, await isSlowEnabled());
}

async function getMostPlayedCached(limit: number, slow: boolean) {
  'use cache';
  cacheTag('tracks');
  await delay(700, slow);
  const rows = await backendFetch<unknown>(`/api/contents/discover/popular?size=${limit}`);
  return unwrapList<BackendTrack>(rows).map(toBackendTrack);
}

export async function getDiscover(limit: number = 8) {
  const userId = await verifyAuth();
  return getDiscoverForUser(userId, limit, await isSlowEnabled());
}

async function getDiscoverForUser(userId: string, limit: number, slow: boolean) {
  'use cache';
  cacheTag(`discover:${userId}`);
  await delay(1100, slow);
  const rows = await backendFetch<unknown>(`/api/contents/discover/for-you?size=${limit}`);
  return unwrapList<BackendTrack>(rows).map(toBackendTrack);
}

export async function getTracksByGenre(genre: string) {
  return getTracksByGenreCached(genre, await isSlowEnabled());
}

async function getTracksByGenreCached(genre: string, slow: boolean) {
  'use cache';
  cacheTag('tracks', `genre-${genre}`);
  await delay(900, slow);
  const rows = await backendFetch<unknown>('/api/contents/tracks?size=100');
  return unwrapList<BackendTrack>(rows)
    .map(toBackendTrack)
    .filter(track => track.genre === genre);
}

export async function getRecommendedTracks(excludeTrackId: string, limit: number = 5) {
  const userId = await verifyAuth();
  return getRecommendedTracksForUser(excludeTrackId, userId, limit, await isSlowEnabled());
}

async function getRecommendedTracksForUser(excludeTrackId: string, userId: string, limit: number, slow: boolean) {
  'use cache';
  cacheTag(`recommendations:${userId}`);
  await delay(900, slow);
  const rows = await backendFetch<unknown>(`/api/contents/discover/for-you?size=${limit + 1}`);
  return unwrapList<BackendTrack>(rows)
    .map(toBackendTrack)
    .filter(track => track.id !== excludeTrackId)
    .slice(0, limit);
}

export async function searchTracks(query: string) {
  return searchTracksCached(query, await isSlowEnabled());
}

async function searchTracksCached(query: string, slow: boolean) {
  'use cache';
  cacheTag('search');
  cacheLife('hours');
  await delay(800, slow);
  const rows = await backendFetch<unknown>(
    `/api/contents/search?q=${encodeURIComponent(query)}&type=TRACK&size=30`,
    {},
    { auth: false },
  );
  return unwrapList<BackendTrack>(rows).map(toBackendTrack);
}
