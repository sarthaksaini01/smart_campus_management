export default function ProgressBar({ value, tone = 'primary' }) {
  const width = Math.max(0, Math.min(100, value));
  return (
    <div className="progress">
      <div className={`progress-bar progress-${tone}`} style={{ width: `${width}%` }} />
    </div>
  );
}