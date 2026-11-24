import { revalidateTag } from 'next/cache';
import { NextResponse, type NextRequest } from 'next/server';
import { z } from 'zod';
import { getCurrentUser } from '@/features/user/user-queries';
import { backendFetch } from '@/lib/backend';

const bodySchema = z.object({
  trackId: z.string().min(1),
});

export async function POST(request: NextRequest) {
  const userId = await getCurrentUser();
  if (!userId) return new NextResponse(null, { status: 401 });

  const parsed = bodySchema.safeParse(await request.json().catch(() => null));
  if (!parsed.success) return new NextResponse(null, { status: 400 });
  const { trackId } = parsed.data;

  await backendFetch(
    `/api/contents/tracks/${trackId}/play`,
    {
      body: JSON.stringify({ positionSeconds: 0 }),
      method: 'POST',
    },
  );

  revalidateTag(`recently-played:${userId}`, 'max');
  revalidateTag(`discover:${userId}`, 'max');
  revalidateTag(`recommendations:${userId}`, 'max');

  return new NextResponse(null, { status: 204 });
}
