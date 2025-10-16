import type { ReceivedMessage } from "../types/chat";
import api from "./instance";

export const fetchChatHistoryAPI = async (roomId: string) => {
  try {
    const response = await api.get(
      `/api/chat/history/${roomId}?page=0&size=50`
    );

    return response.data.content as ReceivedMessage[];
  } catch (err) {
    console.error("Fetch Chat History API Error: ", err);
    throw err;
  }
};
