import React from "react";
import { defaultInstance } from "../../../../api/axios";
import useLoginUserInfoStore from "../../../../store/useLoginUserInfoStore";
import useCompetitionInfoStore from "../../../../store/useCompetitionInfoStore";

const StockSellModal = ({
  closeModal,
  price,
  quantity,
  isMarketPrice,
  companyId,
}) => {
  const loginUserInfo = useLoginUserInfoStore((state) => state.loginUserInfo);
  const setLoginUserInfo = useLoginUserInfoStore(
    (state) => state.setLoginUserInfo
  );
  // const addUnexecutedOrder = useLoginUserInfoStore((state) => state.addUnexecutedOrder); // 미체결 주문 추가 함수

  const competitionInfo = useCompetitionInfoStore(
    (state) => state.competitionInfo
  );

  const orderSellLimit = async () => {
    try {
      const response = await defaultInstance.post(
        "/account/v2/sell/limit",
        {
          accountId: loginUserInfo.accountId,
          memberId: loginUserInfo.loginId,
          companyId: companyId,
          competitionId: competitionInfo.competitionId,
          unitCost: price,
          volume: quantity,
          // accountId: loginUserInfo.accountId,
          // memberId: loginUserInfo.loginId,
          // companyId: companyId,
          // competitionId: competitionInfo.competitionId ,
          // unitCost: price,
          // volume: quantity,
        },
        {
          headers: {
            "Content-Type": "application/json", // 헤더 설정
          },
        }
      );

      if (response.data.status === "SUCCESS") {
        alert("주문을 완료했습니다");

        // 미체결 주문으로 추가
        // addUnexecutedOrder({
        //   companyId,
        //   price,
        //   quantity,
        //   type: "sell",
        //   status: "대기",
        //   createdAt: new Date().toISOString(),
        // });

        // 잔액 바꾸기
        const revenue = price * quantity;

        const newBalance = loginUserInfo.balance + revenue;
        setLoginUserInfo({
          ...loginUserInfo,
          balance: newBalance,
        });

        closeModal();
      } else {
        alert("주문이 실패했습니다."); // 실패 처리
        closeModal();
      }
    } catch (error) {
      console.log(error);
      alert(
        "지정가 주문을 실패했습니다. " +
          (error.response ? error.response.data.message : error.message)
      );
      closeModal();
    }
  };

  const orderSellMarket = async () => {
    try {
      const response = await defaultInstance.post(
        "/account/v2/sell/market",
        {
          accountId: loginUserInfo.accountId,
          memberId: loginUserInfo.loginId,
          companyId: companyId,
          competitionId: competitionInfo.competitionId,
          unitCost: price,
          volume: quantity,
        },
        {
          headers: {
            "Content-Type": "application/json", // 헤더 설정
          },
        }
      );

      if (response.data.status === "SUCCESS") {
        alert("주문을 완료했습니다");

        // 미체결 주문으로 추가
        //  addUnexecutedOrder({
        //   companyId,
        //   price,
        //   quantity,
        //   type: "sell",
        //   status: "대기",
        //   createdAt: new Date().toISOString(),
        // });

        // 잔액 바꾸기
        const revenue = price * quantity;
        const newBalance = loginUserInfo.balance + revenue;
        setLoginUserInfo({
          ...loginUserInfo,
          balance: newBalance,
        });

        closeModal();
      } else {
        alert("주문이 실패했습니다."); // 실패 처리
        closeModal();
      }
    } catch (error) {
      console.log(error);
      alert(
        "시장가 주문을 실패했습니다. " +
          (error.response ? error.response.data.message : error.message)
      );
      closeModal();
    }
  };

  return (
    <div className="font-bold  text-gray-500">
      {quantity == 0 ? (
        <div className="p-8">
          <div className="text-xl text-center font-bold mb-12">
            주문 수량이 없습니다
          </div>
          <button
            className="bg-blue-500 rounded text-white font-bold py-2 w-full"
            onClick={closeModal}
          >
            확인
          </button>
        </div>
      ) : (
        <div className="p-10">
          <div>
            <h2 className="text-2xl text-center font-bold mb-10 text-blue-500">
              매도
            </h2>
            <div className="mb-6">
              <div className="flex justify-between py-1">
                <div>주문 단가</div>
                <div>{price} 원</div>
              </div>
              <div className="flex justify-between py-1">
                <div>주문 수량</div>
                <div>{quantity} 주</div>
              </div>
            </div>
            <div className="mb-10">
              <div>주문 총액</div>
              <div className="font-bold text-2xl text-right text-black">
                {(price * quantity).toLocaleString("ko-KR")} 원
              </div>
            </div>
            <div className="text-center mb-8">판매하시겠습니까?</div>
          </div>
          <div className="flex justify-evenly gap-4">
            <button
              className="bg-blue-100 rounded font-bold text-blue-500 py-2 w-full"
              onClick={closeModal}
            >
              취소
            </button>
            <button
              className="bg-blue-500 rounded text-white font-bold py-2 w-full"
              onClick={() => {
                isMarketPrice ? orderSellMarket() : orderSellLimit();
              }}
              // onClick에 주문 요청 보내기 axios
            >
              예
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default StockSellModal;
