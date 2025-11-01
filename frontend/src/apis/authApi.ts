import api from "./instance";
import type { UserLoginInfo, UserSignupInfo } from "../types/user";

export const loginAPI = async (loginInfo: UserLoginInfo) => {
  try {
    const response = await api.post("/auth/login", loginInfo);
    return response.data;
  } catch (err) {
    console.log("Login API error: ", err);
    throw err;
  }
};

export const signupAPI = async (signupInfo: UserSignupInfo) => {
  try {
    const response = await api.post("/auth/signup", signupInfo);
    return response.data;
  } catch (err) {
    console.log("Signup API error: ", err);
    throw err;
  }
};

export const checkNicknameAPI = async (nickname: string) => {
  try {
    const response = await api.get(`/auth/check-nickname?nickname=${nickname}`);
    return response.data;
  } catch (err) {
    console.log("Check nickname API error: ", err);
    throw err;
  }
};

export const checkEmailAPI = async (email: string) => {
  try {
    const response = await api.get(`/auth/check-email?email=${email}`);
    return response.data;
  } catch (err) {
    console.log("Check email API error: ", err);
    throw err;
  }
};
