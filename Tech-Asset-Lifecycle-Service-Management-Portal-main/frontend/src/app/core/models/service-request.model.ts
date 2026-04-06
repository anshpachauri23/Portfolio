export type RequestStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'WAITING_FOR_USER'
  | 'COMPLETED'
  | 'CLOSED'
  | 'REJECTED';

export type RequestPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface RequestType {
  id: number;
  name: string;
  description: string | null;
  approvalRequired: boolean;
  active: boolean;
}

export interface RequestComment {
  id: number;
  authorId: number;
  authorName: string;
  body: string;
  createdAt: string;
}

export interface ServiceRequest {
  id: number;
  requestNumber: string;
  requestTypeId: number;
  requestTypeName: string;
  title: string;
  description: string;
  requesterId: number;
  requesterName: string;
  assetId: number | null;
  priority: RequestPriority;
  status: RequestStatus;
  assignedToId: number | null;
  assignedToName: string | null;
  approvalRequired: boolean;
  dueDate: string | null;
  closedAt: string | null;
  createdAt: string;
  updatedAt: string;
  comments: RequestComment[];
}

export interface CreateServiceRequestRequest {
  requestTypeId: number;
  title: string;
  description: string;
  assetId?: number;
  priority: RequestPriority;
  dueDate?: string;
}

export interface UpdateRequestStatusRequest {
  status: RequestStatus;
  notes?: string;
}

export interface AddCommentRequest {
  body: string;
}
