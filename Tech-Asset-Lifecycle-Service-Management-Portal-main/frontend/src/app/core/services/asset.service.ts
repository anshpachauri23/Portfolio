import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { z } from 'zod';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import {
  Asset, AssetHistory, CreateAssetRequest,
  UpdateAssetStatusRequest, AssignAssetRequest,
} from '../models/asset.model';

const AssetSchema = z.object({
  id: z.number(),
  assetTag: z.string(),
  serialNumber: z.string().nullable(),
  assetType: z.string(),
  vendor: z.string().nullable(),
  model: z.string().nullable(),
  status: z.string(),
  purchaseDate: z.string().nullable(),
  warrantyExpiry: z.string().nullable(),
  assignedUserId: z.number().nullable(),
  assignedUserName: z.string().nullable(),
  location: z.string().nullable(),
  costCenter: z.string().nullable(),
  notes: z.string().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly baseUrl = `${environment.apiUrl}/api/assets`;

  constructor(private readonly http: HttpClient) {}

  getAssets(params: {
    status?: string;
    assetType?: string;
    assignedUserId?: number;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<ApiResponse<PagedResponse<Asset>>> {
    let httpParams = new HttpParams();
    if (params.status)         httpParams = httpParams.set('status', params.status);
    if (params.assetType)      httpParams = httpParams.set('assetType', params.assetType);
    if (params.assignedUserId) httpParams = httpParams.set('assignedUserId', params.assignedUserId);
    if (params.page != null)   httpParams = httpParams.set('page', params.page);
    if (params.size != null)   httpParams = httpParams.set('size', params.size);
    if (params.sort)           httpParams = httpParams.set('sort', params.sort);

    return this.http.get<ApiResponse<PagedResponse<Asset>>>(this.baseUrl, { params: httpParams });
  }

  getAsset(id: number): Observable<Asset> {
    return this.http
      .get<ApiResponse<unknown>>(`${this.baseUrl}/${id}`)
      .pipe(map(r => AssetSchema.parse(r.data) as Asset));
  }

  createAsset(request: CreateAssetRequest): Observable<Asset> {
    return this.http
      .post<ApiResponse<unknown>>(this.baseUrl, request)
      .pipe(map(r => AssetSchema.parse(r.data) as Asset));
  }

  updateAsset(id: number, request: CreateAssetRequest): Observable<Asset> {
    return this.http
      .put<ApiResponse<unknown>>(`${this.baseUrl}/${id}`, request)
      .pipe(map(r => AssetSchema.parse(r.data) as Asset));
  }

  updateStatus(id: number, request: UpdateAssetStatusRequest): Observable<Asset> {
    return this.http
      .patch<ApiResponse<unknown>>(`${this.baseUrl}/${id}/status`, request)
      .pipe(map(r => AssetSchema.parse(r.data) as Asset));
  }

  assignAsset(id: number, request: AssignAssetRequest): Observable<Asset> {
    return this.http
      .post<ApiResponse<unknown>>(`${this.baseUrl}/${id}/assign`, request)
      .pipe(map(r => AssetSchema.parse(r.data) as Asset));
  }

  getHistory(id: number): Observable<AssetHistory[]> {
    return this.http
      .get<ApiResponse<AssetHistory[]>>(`${this.baseUrl}/${id}/history`)
      .pipe(map(r => r.data));
  }
}
