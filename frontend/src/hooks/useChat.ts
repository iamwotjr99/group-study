import { useEffect, useRef, useState } from "react";
import { useUserStore } from "../store/userStore";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { ReceivedMessage, SendMessagePayload } from "../types/chat";
import { fetchChatHistoryAPI } from "../apis/chatApi";

export const useChat = (roomId: string | undefined) => {
  const [messages, setMessages] = useState<ReceivedMessage[]>([]);
  const { userInfo } = useUserStore();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!roomId || !userInfo) {
      return;
    }

    const loadChatHistroyAndConnect = async () => {
      try {
        const chatHistory = await fetchChatHistoryAPI(roomId);

        setMessages(chatHistory.reverse());
        console.log("history: ", chatHistory);
      } catch (err) {
        console.error("Failed to load chat history in component:", err);
      }

      const accessToken = localStorage.getItem("accessToken");

      // STOMP Client 생성
      const client = new Client({
        webSocketFactory: () => new SockJS("http://localhost:8080/ws/chat"),
        connectHeaders: {
          Authorization: `Bearer ${accessToken}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: (str) => {
          console.log(new Date(), str);
        },
        // STOMP 프로토콜 레벨 에러 핸들러
        onStompError: (frame) => {
          console.error("Broker reported error: " + frame.headers["message"]);
          console.error("Additional details: " + frame.body);
        },

        // WebSocket 자체의 연결 에러 핸들러
        onWebSocketError: (error) => {
          console.error("WebSocket Error:", error);
        },
        onConnect: () => {
          console.log("STOMP 연결");

          // 채팅방 구독
          client.subscribe(`/sub/chatroom/${roomId}`, (message: IMessage) => {
            const newMessage = JSON.parse(message.body) as ReceivedMessage;
            if (newMessage.senderId == userInfo?.memberId) {
              return;
            }
            setMessages((prevMessages) => [...prevMessages, newMessage]);
          });
        },

        onDisconnect: () => {
          console.log("STOMP 연결 해제");
        },
      });

      client.activate();
      clientRef.current = client;
    };

    loadChatHistroyAndConnect();

    return () => {
      clientRef.current?.deactivate();
    };
  }, [roomId, userInfo]);

  const sendMessage = (messageText: string) => {
    console.log("Sending message:", {
      isConnected: clientRef.current?.connected,
      userInfo: !!userInfo, // 객체가 있는지 boolean 값으로 확인
      roomId: roomId,
    });

    if (clientRef.current?.connected && userInfo && roomId) {
      const optimisticMessage: ReceivedMessage = {
        senderId: userInfo?.memberId,
        nickname: userInfo?.nickname,
        content: messageText,
        timestamp: new Date().toISOString(),
      };

      setMessages((prevMessage) => [...prevMessage, optimisticMessage]);

      const messagePayload: SendMessagePayload = {
        message: messageText,
        type: "CHAT",
      };
      clientRef.current.publish({
        destination: `/pub/chatroom/${roomId}/message`,
        body: JSON.stringify(messagePayload),
      });
    }
  };

  return { messages, sendMessage };
};
