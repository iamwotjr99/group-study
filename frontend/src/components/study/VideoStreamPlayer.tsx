import React, { useEffect, useRef } from "react";

interface VideoStreamPlayerProps {
  stream: MediaStream;
  nickname: String;
  isMuted: boolean;
}

function VideoStreamPlayer({
  stream,
  nickname,
  isMuted,
}: VideoStreamPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    if (videoRef.current && stream) {
      if (videoRef.current.srcObject !== stream) {
        videoRef.current.srcObject = stream;
      }
    }
  }, [stream]);

  return (
    <div className="relative bg-black rounded-lg aspect-video flex items-center justify-center overflow-hidden">
      <video
        ref={videoRef}
        className={`w-full h-full object-cover ${
          isMuted ? "-scale-x-100" : ""
        }`}
        autoPlay
        muted={isMuted}
        playsInline
      />
      <div className="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-sm px-2 py-1 rounded">
        {nickname}
      </div>
    </div>
  );
}

export default React.memo(VideoStreamPlayer);
