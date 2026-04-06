import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

export interface AttachmentResponse {
  id: number;
  fileName: string;
  fileSize: number;
  contentType: string | null;
  uploadedById: number;
  uploadedByName: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AttachmentService {
  private readonly baseUrl = `${environment.apiUrl}/api`;

  constructor(private readonly http: HttpClient) {}

  uploadToRequest(requestId: number, file: File): Observable<AttachmentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<AttachmentResponse>>(`${this.baseUrl}/requests/${requestId}/attachments`, formData)
      .pipe(map(r => r.data));
  }

  listRequestAttachments(requestId: number): Observable<AttachmentResponse[]> {
    return this.http
      .get<ApiResponse<AttachmentResponse[]>>(`${this.baseUrl}/requests/${requestId}/attachments`)
      .pipe(map(r => r.data));
  }

  getDownloadUrl(attachmentId: number): string {
    return `${this.baseUrl}/attachments/${attachmentId}/download`;
  }
}
