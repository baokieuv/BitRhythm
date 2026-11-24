export type Track = {
  id: string;
  title: string;
  artist: string;
  album: string;
  duration: number;
  genre: string;
  coverColor: string;
  playCount: number;
  isFavorite: boolean;
  lastPlayedAt: Date | null;
  createdAt: Date;
};

export type BackendTrack = {
  id: number | string;
  title: string;
  artistName?: string;
  artist?: string;
  albumTitle?: string;
  album?: string;
  genre?: string;
  coverUrl?: string;
  coverColor?: string;
  durationSeconds?: number;
  duration?: number;
  playCount?: number;
  liked?: boolean;
  publishedAt?: string;
};

export function toBackendTrack(row: BackendTrack): Track {
  return {
    album: row.albumTitle ?? row.album ?? '',
    artist: row.artistName ?? row.artist ?? '',
    coverColor: row.coverColor ?? 'from-violet-500 to-purple-600',
    createdAt: row.publishedAt ? new Date(row.publishedAt) : new Date(0),
    duration: row.durationSeconds ?? row.duration ?? 0,
    genre: row.genre ?? '',
    id: String(row.id),
    isFavorite: row.liked ?? false,
    lastPlayedAt: null,
    playCount: row.playCount ?? 0,
    title: row.title,
  };
}
