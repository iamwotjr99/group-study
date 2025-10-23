interface SignalPayload {
  type?: "offer" | "answer"; // SDP type
  sdp?: string; // SDP string
  candidate?: string; // ICE Candidate string
  sdpMid?: string | null;
  sdpMLineIndex?: number | null;
}

export interface SignalMessage {
  type: string;
  payload: SignalPayload;
  senderId: number;
  receiverId: number;
}
