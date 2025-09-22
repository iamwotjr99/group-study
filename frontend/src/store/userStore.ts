import { create } from "zustand";

interface UserInfo {
  memberId: number;
  nickname: string;
}

interface UserState {
  accessToken: string | null;
  userInfo: UserInfo | null;
  setUser: (token: string, info: UserInfo) => void;
  clearUser: () => void;
}

export const useUserStore = create<UserState>((set) => ({
  accessToken: null,
  userInfo: null,
  setUser: (token, info) => {
    localStorage.setItem("accessToken", token);
    localStorage.setItem("userInfo", JSON.stringify(info));
    set({ accessToken: token, userInfo: info });
  },
  clearUser: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userInfo");
    set({ accessToken: null });
  },
}));
