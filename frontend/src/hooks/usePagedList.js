import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';

export function usePagedList(basePath, { search, page, size }) {
  const [version, setVersion] = useState(0);
  const [result, setResult] = useState({ key: null, data: null, error: '' });

  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (search) {
    params.set('search', search);
  }
  const url = `${basePath}?${params.toString()}`;
  const key = `${url}#${version}`;

  useEffect(() => {
    let cancelled = false;
    api(url)
      .then((data) => {
        if (!cancelled) setResult({ key, data, error: '' });
      })
      .catch((err) => {
        if (!cancelled) setResult({ key, data: null, error: err.message });
      });
    return () => {
      cancelled = true;
    };
  }, [url, key]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);
  const loading = result.key !== key;

  return { data: result.data, loading, error: loading ? '' : result.error, reload };
}