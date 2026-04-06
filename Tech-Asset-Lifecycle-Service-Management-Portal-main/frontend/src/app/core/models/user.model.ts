export type Role = 'EMPLOYEE' | 'TECHNICIAN' | 'MANAGER' | 'ADMIN';

export interface User {
  id: number;
  name: string;
  email: string;
  employeeCode: string | null;
  role: Role;
  departmentId: number | null;
  departmentName: string | null;
}

export interface JwtPayload {
  sub: string;
  userId: number;
  role: Role;
  iat: number;
  exp: number;
}
