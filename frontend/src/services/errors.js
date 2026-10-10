// Turns an axios error into a message a person can act on.
export function errorMessage(err, fallback = 'Something went wrong. Please try again.') {
  if (!err?.response) return 'Cannot reach the server. Make sure the backend is running on port 8080.';
  const { status, data } = err.response;
  if (status === 429) return 'Too many attempts. Please wait a minute and try again.';
  if (status === 401) return 'Invalid email or password.';
  return data?.message || fallback;
}
