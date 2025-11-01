import { useEffect, useRef, useState } from "react";
import { useUserStore } from "../store/userStore";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type {
  OnlineParticipant,
  ReceivedMessage,
  SendMessagePayload,
} from "../types/chat";
import { fetchChatHistoryAPI } from "../apis/chatApi";

export const useChat = (
  roomId: string | undefined,
  memberId: number | undefined
) => {
  const [messages, setMessages] = useState<ReceivedMessage[]>([]);
  const [onlineParticipants, setOnlineParticipants] = useState<
    OnlineParticipant[]
  >([]);
  const nickname = useUserStore((state) => state.userInfo?.nickname);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!roomId || !memberId) {
      setOnlineParticipants([]);
      return;
    }

    const loadChatHistory = async () => {
      try {
        const chatHistory = await fetchChatHistoryAPI(roomId);
        setMessages(chatHistory.reverse());
        console.log("history: ", chatHistory);
      } catch (err) {
        console.error("채팅 내역 로딩 실패: ", err);
      }
    };

    loadChatHistory();

    const accessToken = localStorage.getItem("accessToken");

    // STOMP Client 생성
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws/chat"),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      // debug: (str) => {
      //   console.log(new Date(), str);
      // },
      // //STOMP 프로토콜 레벨 에러 핸들러
      // onStompError: (frame) => {
      //   console.error("Broker reported error: " + frame.headers["message"]);
      //   console.error("Additional details: " + frame.body);
      // },

      // // WebSocket 자체의 연결 에러 핸들러
      // onWebSocketError: (error) => {
      //   console.error("WebSocket Error:", error);
      // },

      onConnect: () => {
        console.log("STOMP 연결");

        // 채팅 메세지 구독
        client.subscribe(`/sub/chatroom/${roomId}`, (message: IMessage) => {
          const newMessage = JSON.parse(message.body) as ReceivedMessage;
          if (newMessage.senderId == memberId) {
            return;
          }
          setMessages((prevMessages) => [...prevMessages, newMessage]);
        });

        // 최신 온라인 명단 구독
        client.subscribe(
          `/sub/chatroom/${roomId}/participants`,
          (message: IMessage) => {
            const latestParticipants = JSON.parse(
              message.body
            ) as OnlineParticipant[];
            setOnlineParticipants(latestParticipants);
            console.log("최신 온라인 명단: ", latestParticipants);
          }
        );

        // 모든 구독이 완료된 후 최신 온라인 명단 요청
        client.publish({
          destination: `/pub/chatroom/${roomId}/request-participants`,
        });
      },

      onDisconnect: () => {
        console.log("STOMP 연결 해제");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (client?.active) {
        client.deactivate();
      }
    };
  }, [roomId, memberId]);

  const sendMessage = (messageText: string) => {
    console.log("Sending message:", {
      isConnected: clientRef.current?.connected,
      senderId: memberId, // 객체가 있는지 boolean 값으로 확인
      roomId: roomId,
    });

    if (clientRef.current?.connected && nickname && memberId && roomId) {
      const optimisticMessage: ReceivedMessage = {
        senderId: memberId,
        nickname: nickname,
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

  const disconnect = () => {
    if (clientRef.current) {
      clientRef.current.deactivate();
      console.log("STOMP 연결 해제");
    }
  };

  return { messages, onlineParticipants, sendMessage, disconnect };
};
