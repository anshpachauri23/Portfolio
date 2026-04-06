export type AssetStatus =
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'UNDER_REPAIR'
  | 'RECLAIMED'
  | 'LOST'
  | 'RETIRED';

export const ASSET_STATUS_TRANSITIONS: Record<AssetStatus, AssetStatus[]> = {
  AVAILABLE:    ['ASSIGNED', 'UNDER_REPAIR', 'RETIRED'],
  ASSIGNED:     ['AVAILABLE', 'UNDER_REPAIR', 'RECLAIMED', 'LOST'],
  UNDER_REPAIR: ['AVAILABLE', 'RETIRED'],
  RECLAIMED:    ['AVAILABLE', 'RETIRED'],
  LOST:         ['RECLAIMED', 'RETIRED'],
  RETIRED:      [],
};

export interface Asset {
  id: number;
  assetTag: string;
  serialNumber: string | null;
  assetType: string;
  vendor: string | null;
  model: string | null;
  status: AssetStatus;
  purchaseDate: string | null;
  warrantyExpiry: string | null;
  assignedUserId: number | null;
  assignedUserName: string | null;
  location: string | null;
  costCenter: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AssetHistory {
  id: number;
  eventType: string;
  fromStatus: string | null;
  toStatus: string | null;
  actorId: number | null;
  actorName: string | null;
  notes: string | null;
  createdAt: string;
}

export interface CreateAssetRequest {
  assetTag: string;
  serialNumber?: string;
  assetType: string;
  vendor?: string;
  model?: string;
  purchaseDate?: string;
  warrantyExpiry?: string;
  location?: string;
  costCenter?: string;
  notes?: string;
}

export interface UpdateAssetStatusRequest {
  status: AssetStatus;
  notes?: string;
}

export interface AssignAssetRequest {
  userId: number;
  notes?: string;
}
