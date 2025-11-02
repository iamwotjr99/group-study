import React, { useState } from "react";

interface ChatFormProps {
  onSendMessage: (message: string) => void;
}

function ChatForm({ onSendMessage }: ChatFormProps) {
  const [newMessage, setNewMessage] = useState("");

  const handleSendMessage = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (newMessage.trim() !== "") {
      onSendMessage(newMessage);
      setNewMessage("");
    }
  };

  return (
    <form
      className="p-4 border-t border-gray-200 flex"
      onSubmit={handleSendMessage}
    >
      <input
        type="text"
        className="flex-1 border border-gray-300 rounded-l-md p-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        placeholder="메시지 입력..."
        value={newMessage}
        onChange={(e) => setNewMessage(e.target.value)}
      />
      <button
        type="submit"
        className="bg-indigo-600 text-white px-4 rounded-r-md hover:bg-indigo-700"
      >
        전송
      </button>
    </form>
  );
}

export default ChatForm;
