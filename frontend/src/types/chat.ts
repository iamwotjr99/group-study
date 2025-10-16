export type MessageType = "ENTER" | "CHAT" | "LEAVE";

export interface SendMessagePayload {
  message: string;
  type: MessageType;
}

export interface ReceivedMessage {
  senderId: number;
  nickname: string;
  content: string;
  timestamp: string;
}
