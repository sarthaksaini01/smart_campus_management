import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';

export function useFetch(path) {
  const [version, setVersion] = useState(0);
  const [result, setResult] = useState({ key: null, data: null, error: '' });
  const key = `${path}#${version}`;

  useEffect(() => {
    let cancelled = false;
    api(path)
      .then((data) => {
        if (!cancelled) setResult({ key, data, error: '' });
      })
      .catch((err) => {
        if (!cancelled) setResult({ key, data: null, error: err.message });
      });
    return () => {
      cancelled = true;
    };
  }, [path, key]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);
  const loading = result.key !== key;

  return { data: result.data, loading, error: loading ? '' : result.error, reload };
}