export const MIN_ATTENDANCE = 75;

export function attendanceTone(percentage, total) {
  if (total === 0) return undefined;
  if (percentage < MIN_ATTENDANCE) return 'danger';
  if (percentage < 85) return 'warning';
  return 'success';
}

/**
 * kind 'short': number of classes to attend in a row to reach the minimum.
 * kind 'ok': number of classes that can still be missed while staying at the minimum.
 */
export function attendanceAdvice(present, total) {
  if (total === 0) {
    return null;
  }
  const percentage = (present * 100) / total;
  if (percentage < MIN_ATTENDANCE) {
    return { kind: 'short', count: Math.max(0, 3 * total - 4 * present) };
  }
  return { kind: 'ok', count: Math.max(0, Math.floor((4 * present - 3 * total) / 3)) };
}

export function adviceText(present, total) {
  const advice = attendanceAdvice(present, total);
  if (!advice) {
    return 'No classes yet';
  }
  const classes = `${advice.count} class${advice.count === 1 ? '' : 'es'}`;
  if (advice.kind === 'short') {
    return `Attend the next ${classes} in a row to reach ${MIN_ATTENDANCE}%`;
  }
  if (advice.count === 0) {
    return `You are right at the ${MIN_ATTENDANCE}% limit. Try not to miss any class`;
  }
  return `You can miss up to ${classes} and stay above ${MIN_ATTENDANCE}%`;
}