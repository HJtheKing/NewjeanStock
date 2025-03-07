import React from "react";
import { defaultInstance } from "../../../../api/axios";
import useLoginUserInfoStore from "../../../../store/useLoginUserInfoStore";
import useCompetitionInfoStore from "../../../../store/useCompetitionInfoStore";

const StockBuyModal = ({
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

  const orderBuyLimit = async () => {
    try {
      console.log(loginUserInfo.accountId);
      console.log(loginUserInfo.loginId);
      console.log(companyId);
      console.log(competitionInfo.competitionId);
      const response = await defaultInstance.post(
        "/account/v2/buy/limit",
        {
          // accountId: 11,
          // memberId: 11,
          // companyId: companyId,
          // competitionId: 2,
          // unitCost: price,
          // volume: quantity,
          accountId: loginUserInfo.accountId,
          memberId: loginUserInfo.loginId,
          companyId: companyId,
          competitionId: competitionInfo.competitionId,
          unitCost: price,
          volume: quantity,
          margin: true, // 추가된 margin 필드
        },
        {
          headers: {
            "Content-Type": "application/json",
            access_token: loginUserInfo.accessToken, // access_token 추가
          },
        }
      );

      // 성공 시 응답 처리
      if (response.data.status === "SUCCESS") {
        console.log(response.data);
        alert("주문을 완료했습니다");

        // 미체결 주문으로 추가
        // addUnexecutedOrder({
        //   companyId,
        //   price,
        //   quantity,
        //   type: "buy",
        //   status: "대기",
        //   createdAt: new Date().toISOString(),
        // });

        // 잔액 바꾸기
        // const cost =
        //   response.data.tradeRequest.unitCost *
        //   response.data.tradeRequest.totalOrderVolume;
        // const cost = price * quantity;

        // const newBalance = loginUserInfo.balance - cost;
        const tmp = await defaultInstance.get(
          `/account/v2/${loginUserInfo.accountId}`,
          {
            headers: {
              "Content-Type": "application/json",
              access_token: loginUserInfo.accessToken, // access_token 추가
            },
          }
        );
        console.log(tmp);
        setLoginUserInfo({
          ...loginUserInfo,
          balance: tmp.data.balance,
        });

        closeModal();
      } else {
        alert("주문이 실패했습니다.");
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

  const orderBuyMarket = async () => {
    try {
      const response = await defaultInstance.post(
        "/account/v2/buy/market",
        {
          accountId: loginUserInfo.accountId,
          memberId: loginUserInfo.loginId,
          companyId: companyId,
          competitionId: competitionInfo.competitionId,
          unit_cost: price, // 임시로 지정가 매수와 동일 로직으로 구현
          volume: quantity,
          margin: true, // 마진 여부
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (response.data.status === "SUCCESS") {
        alert("주문을 완료했습니다");

        console.log(response.data);
        alert("주문을 완료했습니다");
        // 잔액 바꾸기
        // const cost =
        //   response.data.tradeRequest.unitCost *
        //   response.data.tradeRequest.totalOrderVolume;
        const cost = price * quantity;

        const newBalance = loginUserInfo.balance - cost;
        setLoginUserInfo({
          ...loginUserInfo,
          balance: newBalance,
        });
        closeModal();
      } else {
        alert("주문을 실패했습니다.");
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
    <div className="font-bold text-gray-500">
      {quantity === 0 ? (
        <div className="p-8">
          <div className="text-xl text-center font-bold mb-12">
            주문 수량이 없습니다
          </div>
          <button
            className="bg-red-500 rounded text-white font-bold py-2 w-full"
            onClick={closeModal}
          >
            확인
          </button>
        </div>
      ) : (
        <div className="p-10">
          <div>
            <h2 className="text-2xl text-center font-bold mb-10 text-red-500">
              매수
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
            <div className="text-center mb-8">구매하시겠습니까?</div>
          </div>
          <div className="flex justify-evenly gap-4">
            <button
              className="bg-red-100 rounded font-bold text-red-500 py-2 w-full"
              onClick={closeModal}
            >
              취소
            </button>
            <button
              className="bg-red-500 rounded text-white font-bold py-2 w-full"
              onClick={() => {
                isMarketPrice ? orderBuyMarket() : orderBuyLimit();
              }}
            >
              예
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default StockBuyModal;
