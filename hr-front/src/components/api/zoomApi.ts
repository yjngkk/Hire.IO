import apiService from '../../config/apiService';

export interface ZoomMeetingRequest {
  topic: string;
  type: number;
  start_time: string;
  duration: number;
  timezone: string;
  agenda: string;
  password?: string;
  settings?: {
    host_video: boolean;
    participant_video: boolean;
    join_before_host: boolean;
    mute_upon_entry: boolean;
    watermark: boolean;
    use_pmi: boolean;
    approval_type: number;
    audio: string;
    auto_recording: string;
    enforce_login: boolean;
    close_registration: boolean;
    show_share_button: boolean;
    allow_multiple_devices: boolean;
    registrants_confirmation_email: boolean;
    waiting_room: boolean;
    request_permission_to_unmute_participants: boolean;
    registrants_email_notification: boolean;
    meeting_authentication: boolean;
  };
}

export interface ZoomMeetingResponse {
  uuid: string;
  id: number;
  host_id: string;
  host_email: string;
  topic: string;
  type: number;
  status: string;
  start_time: string;
  duration: number;
  timezone: string;
  agenda: string;
  created_at: string;
  start_url: string;
  join_url: string;
  password: string;
  h323_password: string;
  pstn_password: string;
  encrypted_password: string;
  settings: {
    host_video: boolean;
    participant_video: boolean;
    cn_meeting: boolean;
    in_meeting: boolean;
    join_before_host: boolean;
    mute_upon_entry: boolean;
    watermark: boolean;
    use_pmi: boolean;
    approval_type: number;
    audio: string;
    auto_recording: string;
    enforce_login: boolean;
    enforce_login_domains: string;
    alternative_hosts: string;
    close_registration: boolean;
    show_share_button: boolean;
    allow_multiple_devices: boolean;
    registrants_confirmation_email: boolean;
    waiting_room: boolean;
    request_permission_to_unmute_participants: boolean;
    global_dial_in_countries: string;
    global_dial_in_numbers: string;
    registrants_email_notification: boolean;
    meeting_authentication: boolean;
    authentication_option: string;
    authentication_domains: string;
    authentication_name: string;
    additional_data_center_regions: boolean;
  };
  pre_schedule: boolean;
  occurrences: Array<{
    occurrence_id: string;
    start_time: string;
    duration: number;
    status: string;
  }>;
  recurrence: {
    type: number;
    repeat_interval: number;
    weekly_days: string;
    monthly_day: number;
    monthly_week: number;
    monthly_week_day: number;
    end_times: number;
    end_date_time: string;
  };
}

export interface ZoomTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
  scope: string;
}

export const zoomApi = {
  // Créer une réunion Zoom
  createMeeting: async (meetingRequest: ZoomMeetingRequest): Promise<ZoomMeetingResponse> => {
    return apiService.post('/zoom/meetings', meetingRequest);
  },

  // Créer une réunion Zoom pour un entretien
  createInterviewMeeting: async (
    candidateName: string,
    jobTitle: string,
    startTime: string,
    duration: number,
    agenda?: string
  ): Promise<ZoomMeetingResponse> => {
    const params = new URLSearchParams({
      candidateName,
      jobTitle,
      startTime,
      duration: duration.toString(),
      ...(agenda && { agenda })
    });
    
    return apiService.post(`/zoom/meetings/interview?${params}`);
  },

  // Récupérer les détails d'une réunion Zoom
  getMeeting: async (meetingId: number): Promise<ZoomMeetingResponse> => {
    return apiService.get(`/zoom/meetings/${meetingId}`);
  },

  // Mettre à jour une réunion Zoom
  updateMeeting: async (meetingId: number, meetingRequest: ZoomMeetingRequest): Promise<void> => {
    return apiService.put(`/zoom/meetings/${meetingId}`, meetingRequest);
  },

  // Supprimer une réunion Zoom
  deleteMeeting: async (meetingId: number): Promise<void> => {
    return apiService.delete(`/zoom/meetings/${meetingId}`);
  },

  // Lister les réunions d'un utilisateur
  listMeetings: async (
    userId: string = 'me',
    type?: string,
    pageSize: number = 30,
    nextPageToken?: string
  ): Promise<any> => {
    const params = new URLSearchParams({
      userId,
      pageSize: pageSize.toString(),
      ...(type && { type }),
      ...(nextPageToken && { nextPageToken })
    });
    
    return apiService.get(`/zoom/meetings?${params}`);
  },

  // Tester la connexion à l'API Zoom
  testConnection: async (): Promise<{ status: string; message: string; token?: string }> => {
    return apiService.get('/zoom/test');
  },

  // Créer une réunion Zoom pour un entretien existant
  createZoomForInterview: async (interviewId: number): Promise<{
    success: boolean;
    message: string;
    meetingId?: number;
    joinUrl?: string;
    startUrl?: string;
    password?: string;
  }> => {
    return apiService.post(`/entretiens/${interviewId}/zoom`);
  }
};
