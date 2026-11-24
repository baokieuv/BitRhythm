'use client';

import { useActionState } from 'react';
import { Button } from '@/components/ui/button';
import { signIn } from '@/features/user/user-actions';

type State = { error?: string } | null;

async function action(_prev: State, formData: FormData): Promise<State> {
  const result = await signIn(formData);
  if (result && !result.ok) return { error: result.error };
  return null;
}

export function SignInForm() {
  const [state, formAction] = useActionState(action, null);

  return (
    <form action={formAction} className="flex flex-col gap-3">
      <label htmlFor="identifier" className="text-muted text-sm">
        Username or email
      </label>
      <input
        id="identifier"
        name="identifier"
        type="text"
        autoComplete="username"
        required
        autoFocus
        placeholder="your username or email"
        aria-invalid={state?.error ? true : undefined}
        aria-describedby={state?.error ? 'sign-in-hint sign-in-error' : 'sign-in-hint'}
      />
      <label htmlFor="password" className="text-muted text-sm">
        Password
      </label>
      <input
        id="password"
        name="password"
        type="password"
        autoComplete="current-password"
        required
        minLength={1}
        aria-invalid={state?.error ? true : undefined}
        aria-describedby={state?.error ? 'sign-in-hint sign-in-error' : 'sign-in-hint'}
      />
      <p id="sign-in-hint" className="text-muted -mt-1 text-xs">
        Use your BitRhythm account.
      </p>
      {state?.error ? (
        <p id="sign-in-error" role="alert" className="text-sm text-red-500">
          {state.error}
        </p>
      ) : null}
      <Button type="submit" className="w-full">
        Sign in
      </Button>
    </form>
  );
}
