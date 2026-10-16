const DASHBOARD = { to: '/', label: 'Dashboard', end: true };
const NOTICES = { to: '/notices', label: 'Notices' };

export const NAV = {
  ADMIN: [
    DASHBOARD,
    { to: '/admin/students', label: 'Students' },
    { to: '/admin/faculty', label: 'Faculty' },
    { to: '/admin/courses', label: 'Courses' },
    { to: '/admin/subjects', label: 'Subjects' },
    NOTICES,
  ],
  FACULTY: [
    DASHBOARD,
    { to: '/faculty/subjects', label: 'My subjects' },
    { to: '/faculty/attendance', label: 'Attendance' },
    { to: '/faculty/marks', label: 'Marks' },
    { to: '/faculty/at-risk', label: 'At-risk students' },
    NOTICES,
  ],
  STUDENT: [
    DASHBOARD,
    { to: '/student/attendance', label: 'Attendance' },
    { to: '/student/results', label: 'Results' },
    { to: '/student/insights', label: 'Risk and AI advice' },
    NOTICES,
  ],
};