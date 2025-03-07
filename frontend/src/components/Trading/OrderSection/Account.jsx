import React from "react";
import { useState, useEffect } from "react";
import useStockDataStore from "./../../../store/useStockDataStore";
import useLoginUserInfoStore from "./../../../store/useLoginUserInfoStore";
import { authInstance } from "./../../../api/axios";
import useDarkModeStore from "./../../../store/useDarkModeStore";

const holdings = {
  balance: "5600459",
  holding_list: [
    {
      companyName: "삼성전자",
      price: 3546400,
      volume: 53,
      currentPrice: 77200,
    },
    {
      companyName: "카카오",
      price: 1546500,
      volume: 50,
      currentPrice: 36800,
    },
  ],
};

const Account = () => {
  const stockInfo = useStockDataStore((state) => state.stockInfo);
  const accountId = useLoginUserInfoStore(
    (state) => state.loginUserInfo.accountId
  );
  const [holding, setHolding] = useState([]);
  const [balance, setBalance] = useState(0);
  const [totalProfit, setTotalProfit] = useState(0);
  const [totalProfitRate, setTotalProfitRate] = useState(0);

  const { darkMode } = useDarkModeStore((state) => ({
    darkMode: state.darkMode,
  }));

  useEffect(() => {
    if (accountId) {
      getAccountInfo(accountId);
    }
  }, [accountId]);

  // 계좌 및 보유 주식 조회 API 호출
  const getAccountInfo = async (accountId) => {
    try {
      const response = await authInstance.get(`/account/v2/${accountId}`);
      const stocks = response.data.stocks;
      setBalance(response.data.balance);
      setHolding(stocks);
      calculateTotalProfit(stocks);
    } catch (error) {
      console.error("보유 주식 조회 실패:", error);
    }
  };

  // 총 평가수익금 및 총 평가수익률 계산
  const calculateTotalProfit = (stocks) => {
    let totalPurchasePrice = 0; // 총 매수 금액
    let totalCurrentValue = 0; // 총 현재 평가 금액
    let totalProfit = 0; // 총 평가수익금
    stocks.forEach((stock) => {
      const purchasePrice = stock.price; // 매수 가격
      const currentValue = stock.currentPrice * stock.volume; // 현재 평가 금액
      totalPurchasePrice += purchasePrice;
      totalCurrentValue += currentValue;
      totalProfit += currentValue - purchasePrice; // 수익금 계산
    });
    const profitRate = (totalProfit / totalPurchasePrice) * 100; // 수익률 계산
    setTotalProfit(totalProfit);
    setTotalProfitRate(profitRate);
  };

  return (
    <div className="overflow-y-auto max-h-[450px]">
      {/* 총 잔고 및 평가 금액 */}
      <div className="grid grid-cols-3 gap-2 items-center text-center text-sm p-3 mb-6 font-bold">
        <div>
          <div className="pb-1">{balance.toLocaleString()}원</div>
          <div>총 잔고</div>
        </div>
        <div>
          <div className="pb-1">{totalProfit.toLocaleString()}원</div>
          <div>총 평가수익금</div>
        </div>
        <div>
          <div className="pb-1">
            {totalProfitRate >= 0
              ? `+${totalProfitRate.toFixed(2)}`
              : `${totalProfitRate.toFixed(2)}`}
            %
          </div>
          <div>총 평가수익률</div>
        </div>
      </div>

      {/* 라벨 */}
      <div
        className={`grid grid-cols-5 gap-2 items-center text-right text-sm bg-gray-100 p-3 font-bold ${
          darkMode ? "bg-gray-700 text-gray-200" : "bg-gray-100 text-black"
        }`}
      >
        <div className="col-span-2 text-left">종목</div>
        <div>
          <div className="pb-1">평가금액</div>
          <div>평단가</div>
        </div>
        <div>보유수량</div>
        <div>평가수익률</div>
      </div>

      {/* 보유 주식 리스트 */}
      {holding.length > 0 ? (
        holding.map((stock, index) => (
          <div key={index}>
            <div className="grid grid-cols-5 gap-2 items-center text-right text-sm p-3 font-bold">
              <div className="col-span-2 text-left font-bold">
                {stock.companyId === "1" ? "삼성전자" : "기타 종목"}
              </div>
              <div>
                <div className="pb-1 text-gray-500">
                  {(stock.currentPrice * stock.volume).toLocaleString()}원
                </div>
                <div className="text-gray-500">
                  {(stock.price / stock.volume).toLocaleString()}원
                </div>
              </div>
              <div className="text-gray-500">{stock.volume}주</div>
              <div className="font-bold">
                <span
                  style={{
                    color: stock.currentPrice > stock.price ? "red" : "blue",
                  }}
                >
                  {(
                    ((stock.currentPrice - stock.price) / stock.price) *
                    100
                  ).toFixed(2)}
                  %
                </span>
              </div>
            </div>
            <hr className="mb-2" />
          </div>
        ))
      ) : (
        <div>보유 주식이 없습니다.</div>
      )}
    </div>
  );

export default Account;
