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
  FACULTY: [DASHBOARD, NOTICES],
  STUDENT: [DASHBOARD, NOTICES],
};