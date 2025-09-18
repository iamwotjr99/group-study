import api from "./instance";
import type { UserLoginInfo } from "../types/UserLoginInfo";

export const loginAPI = async (loginInfo : UserLoginInfo) => {
    try {
        const response = await api.post('/api/auth/login', loginInfo);
        return response.data;
    } catch(err) {
        console.log(err);
        throw err;
    }
}