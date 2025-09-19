import { create } from "zustand";

interface UserState {
  accessToken: string | null;
  setToken: (token: string) => void;
  clearToken: () => void;
}

export const useUserStore = create<UserState>((set) => ({
  accessToken: null,
  setToken: (token) => {
    console.log(
      "%cZustand Store: 토큰 설정!",
      "color: green; font-weight: bold;",
      token
    );
    set({ accessToken: token });
  },
  clearToken: () => {
    localStorage.removeItem("accessToken");
    set({ accessToken: null });
  },
}));
