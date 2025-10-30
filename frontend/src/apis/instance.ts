import axios from "axios";
import { useUserStore } from "../store/userStore";

const BASE_URL = import.meta.env.VITE_API_URL;

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (err) => {
    if (err.response?.status === 401) {
      // 401 Unauthorized 에러 (토큰 만료)
      console.error("토큰 유효기간이 만료됐습니다. 자동 로그아웃 됩니다.");

      useUserStore.getState().clearUser();

      // 로그인 페이지로 이동
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default api;
