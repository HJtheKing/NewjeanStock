import { create } from "zustand";
import { persist } from "zustand/middleware";
import axios from "axios";

// hook 기반으로 스토어 생성
const useLoginUserInfoStore = create(
  persist(
    (set) => ({
      // 상태 선언
      loginUserInfo: {
        loginId: null,
        isLogined: null,
        isCompParticipant: null,
        nickname: "",
        rank: "",
        accountId: null,
        balance: null, // 잔고
        rating: "",
        tier: "",
        kakaoUser: null,
      },

      // unexecutedOrders: [], // 미체결 주문 리스트

      // LoginUserInfo 관련 데이터 변경 함수
      setLoginUserInfo: (loginUserInfo) => set({ loginUserInfo }),

      addUnexecutedOrder: (order) =>
        set((state) => ({
          unexecutedOrders: [...state.unexecutedOrders, order],
        })),

      // API를 통해 잔고 업데이트 함수
      updateBalance: async (accountId, token) => {
        try {
          const response = await axios.get(`/account/v2/${accountId}`, {
            headers: {
              "Content-Type": "application/json",
              access_token: token,
            },
          });

          if (response.data && response.data.balance) {
            set((state) => ({
              loginUserInfo: {
                ...state.loginUserInfo,
                balance: response.data.balance, // 잔고 업데이트
              },
            }));
          }
        } catch (error) {
          console.error("Failed to fetch balance", error);
        }
      },

      // 로그인 정보 초기화
      clearLoginUserInfo: () =>
        set({
          loginUserInfo: {
            loginId: null,
            isLogined: null,
            isCompParticipant: null,
            nickname: "",
            rank: "",
            accountId: null,
            balance: null,
            rating: "",
            tier: "",
            kakaoUser: null,
          },
        }),
    }),
    {
      name: "loginUserInfo", // localStorage에 저장되는 항목의 이름
    }
  )
);

export default useLoginUserInfoStore;
