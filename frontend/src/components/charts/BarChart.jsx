export default function BarChart({ data, max, unit = '', height = 190, emptyText = 'No data yet' }) {
  if (data.length === 0) {
    return <p className="muted empty">{emptyText}</p>;
  }

  const top = max ?? Math.max(1, ...data.map((d) => d.value));

  return (
    <div className="bars-scroll">
      <div className="bars" style={{ height }} role="img" aria-label="Bar chart">
        {data.map((d) => (
          <div className="bar-col" key={d.label} title={`${d.label}: ${d.value}${unit}`}>
            <div className="bar-value">
              {d.value}
              {unit}
            </div>
            <div className="bar-track">
              <div
                className={`bar-fill bar-${d.tone ?? 'primary'}`}
                style={{ height: `${Math.min(100, (d.value / top) * 100)}%` }}
              />
            </div>
            <div className="bar-label">{d.label}</div>
          </div>
        ))}
      </div>
    </div>
  );
}