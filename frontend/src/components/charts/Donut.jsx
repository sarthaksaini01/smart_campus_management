const RADIUS = 52;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

export default function Donut({ data, centerLabel = 'total' }) {
  const total = data.reduce((sum, d) => sum + d.value, 0);
  if (total === 0) {
    return <p className="muted empty">No data yet</p>;
  }

  const segments = [];
  let used = 0;
  for (const d of data) {
    if (d.value > 0) {
      const length = (d.value / total) * CIRCUMFERENCE;
      segments.push({ ...d, length, offset: used });
      used += length;
    }
  }

  return (
    <div className="donut-wrap">
      <svg viewBox="0 0 140 140" className="donut" role="img" aria-label="Donut chart">
        <g transform="rotate(-90 70 70)">
          <circle cx="70" cy="70" r={RADIUS} fill="none" stroke="#e5e7eb" strokeWidth="20" />
          {segments.map((s) => (
            <circle
              key={s.label}
              cx="70"
              cy="70"
              r={RADIUS}
              fill="none"
              stroke={s.color}
              strokeWidth="20"
              strokeDasharray={`${s.length} ${CIRCUMFERENCE - s.length}`}
              strokeDashoffset={-s.offset}
            />
          ))}
        </g>
        <text x="70" y="68" textAnchor="middle" className="donut-total">
          {total}
        </text>
        <text x="70" y="85" textAnchor="middle" className="donut-caption">
          {centerLabel}
        </text>
      </svg>

      <ul className="legend">
        {data.map((d) => (
          <li key={d.label}>
            <span className="dot" style={{ background: d.color }} />
            {d.label}: <strong>{d.value}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}