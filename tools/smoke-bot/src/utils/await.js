'use strict';

class TimeoutError extends Error {
  constructor(msg) { super(msg); this.name = 'TimeoutError'; }
}

// Poll predicate every intervalMs until truthy or timeoutMs elapsed.
// Returns last truthy predicate value on success, throws TimeoutError on failure.
async function awaitState(predicate, opts = {}) {
  const timeoutMs  = opts.timeoutMs  || 5000;
  const intervalMs = opts.intervalMs || 200;
  const label      = opts.label      || 'awaitState';
  const startTime  = Date.now();
  let lastError;

  while (Date.now() - startTime < timeoutMs) {
    try {
      const result = await predicate();
      if (result) return result;
    } catch (err) {
      lastError = err;
    }
    await sleep(intervalMs);
  }

  const reason = lastError ? ` (last error: ${lastError.message})` : '';
  throw new TimeoutError(`${label}: timed out after ${timeoutMs}ms${reason}`);
}

// Retry an async fn up to N attempts with exponential backoff.
// Re-throws last error after all attempts exhausted.
async function retry(fn, opts = {}) {
  const attempts     = opts.attempts     || 3;
  const initialDelay = opts.initialDelay || 200;
  const label        = opts.label        || 'retry';
  let lastErr;

  for (let i = 0; i < attempts; i++) {
    try {
      return await fn(i);
    } catch (err) {
      lastErr = err;
      if (i < attempts - 1) await sleep(initialDelay * Math.pow(2, i));
    }
  }
  // Re-throw original error to preserve type (AssertionError → [assertion], TimeoutError → [timeout])
  // Prepend label+attempt count to the message for context
  lastErr.message = `${label}: failed after ${attempts} attempts. Last: ${lastErr.message}`;
  throw lastErr;
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

module.exports = { awaitState, retry, sleep, TimeoutError };
