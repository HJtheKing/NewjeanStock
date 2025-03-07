import React, { useState, useEffect } from "react";
import QuantityButton from "./QuantityButton";
import PriceToggle from "./PriceToggle";
import useLoginUserInfoStore from "./../../../../store/useLoginUserInfoStore";
import useDarkModeStore from "./../../../../store/useDarkModeStore"; // 다크 모드 상태 가져오기
import { authInstance } from "./../../../../api/axios"; // authInstance 가져오기

const OrderStockInput = ({
  activeTabOption,
  setPrice,
  setQuantity,
  price,
  quantity,
  selectedPrice,
  setSelectedPriceState,
  marketPrice,
  isMarketPrice,
  setIsMarketPrice,
}) => {
  const { loginUserInfo, updateBalance, setToken } = useLoginUserInfoStore(
    (state) => ({
      loginUserInfo: state.loginUserInfo,
      updateBalance: state.updateBalance,
      setToken: state.setToken, // JWT 토큰 상태를 저장하는 함수
    })
  );

  const myAccount = loginUserInfo.balance; // 잔고 불러오기

  // JWT 토큰 상태
  const [token, setTokenState] = useState(null);
  const accountId = loginUserInfo.accountId;

  // 컴포넌트가 마운트될 때 JWT 토큰 받아오기
  useEffect(() => {
    const fetchToken = async () => {
      try {
        const response = await authInstance.post("/auth/login", {
          // 필요한 로그인 정보
          username: "user",
          password: "password",
        });
        const token = response.data.token;
        setTokenState(token); // JWT 토큰 상태 저장
        setToken(token); // Zustand에 토큰 저장
      } catch (error) {
        console.error("토큰 가져오기 실패:", error);
      }
    };

    if (!token) {
      fetchToken(); // 토큰이 없으면 가져오기
    }
  }, [token, setToken]);

  // 잔고 업데이트
  useEffect(() => {
    if (accountId && token) {
      updateBalance(accountId, token);
    }
  }, [accountId, token, updateBalance]);

  // 임의의 해당 종목 보유량
  const myQuantity = 200;

  // 증거금률 적용 여부
  const [isMarginApplied, setIsMarginApplied] = useState(false);

  // 선택된 증거금률
  const [selectedMarginRate, setSelectedMarginRate] = useState(10);

  // 보유할 수 있는 최대개수 (증거금률 적용 시 10배, 25배 등 선택한 증거금률에 따라 변경)
  const maxQuantity = isMarginApplied
    ? Math.floor((myAccount / price) * (100 / selectedMarginRate))
    : activeTabOption === "buy"
      ? Math.floor(myAccount / price)
      : myQuantity;

  // 입력할 수 있는 최대 가격
  const maxPrice = Math.floor(myAccount / quantity);

  // 다크 모드 상태 가져오기
  const { darkMode } = useDarkModeStore((state) => ({
    darkMode: state.darkMode,
  }));

  // 실시간으로 주문 총액 및 사용자가 부담해야 하는 금액 계산
  const calculatePayableAmount = () => {
    const totalOrderAmount = price * quantity; // 주문 총액 계산
    const userPayableAmount = isMarginApplied
      ? (totalOrderAmount * selectedMarginRate) / 100 // 미수 거래가 적용된 경우
      : totalOrderAmount; // 일반 거래의 경우
    return { totalOrderAmount, userPayableAmount };
  };

  // 상태 변수로 주문 총액과 사용자가 부담해야 하는 금액 저장
  const [totalOrderAmount, setTotalOrderAmount] = useState(0);
  const [userPayableAmount, setUserPayableAmount] = useState(0);

  useEffect(() => {
    const { totalOrderAmount, userPayableAmount } = calculatePayableAmount();
    setTotalOrderAmount(totalOrderAmount);
    setUserPayableAmount(userPayableAmount);
  }, [price, quantity, isMarginApplied, selectedMarginRate]); // 의존성 추가

  useEffect(() => {
    setPrice(selectedPrice);
  }, [selectedPrice]);

  if (isMarketPrice) {
    setPrice(marketPrice);
    setSelectedPriceState(marketPrice);
  }

  const onChangePrice = (e) => {
    const newPrice = e.target.value;
    if (newPrice >= maxPrice) {
      setPrice(maxPrice);
      setSelectedPriceState(parseInt(maxPrice));
    } else {
      setPrice(newPrice);
      setSelectedPriceState(parseInt(newPrice));
    }
  };

  const onChangeQuantity = (e) => {
    const newQuantity = e.target.value;
    newQuantity >= maxQuantity
      ? setQuantity(maxQuantity)
      : setQuantity(newQuantity);
  };

  const onClickPercent = (percent) => {
    setQuantity(Math.floor((maxQuantity * percent) / 100));
  };

  const toggleMargin = () => {
    setIsMarginApplied((prev) => !prev); // 증거금률 적용 여부 토글
  };

  const handleMarginRateChange = (e) => {
    setSelectedMarginRate(parseInt(e.target.value));
  };

  return (
    <div className="mx-4">
      {/* 가격 */}
      <div className="mb-4 flex flex-col">
        <PriceToggle
          isMarketPrice={isMarketPrice}
          setIsMarketPrice={setIsMarketPrice}
        />
        <label htmlFor="price" className="font-bold text-sm mb-1">
          가격
        </label>
        <div className="flex justify-between py-2 px-1 border border-gray-400 rounded-md">
          <input
            id="price"
            value={price}
            type="number"
            min={0}
            step={100}
            className={`w-full focus:outline-none text-right ${
              isMarketPrice ? "text-gray-300" : darkMode ? "bg-gray-800" : ""
            }`}
            onChange={onChangePrice}
          />
          <span className="text-gray-400 pr-2">원</span>
        </div>
      </div>

      {/* 수량 */}
      <div className="mb-4 flex flex-col">
        <label htmlFor="quantity" className="font-bold text-sm mb-1">
          수량
        </label>
        <div className="flex justify-between py-2 px-1 border border-gray-400 rounded-md">
          <input
            id="quantity"
            value={quantity}
            type="number"
            min={0}
            className={`w-full focus:outline-none text-right ${
              darkMode ? "bg-gray-800" : ""
            }`}
            onChange={onChangeQuantity}
          />
          <span className="text-gray-400 pr-2">주</span>
        </div>
      </div>

      {/* 수량 퍼센트 버튼 */}
      <div
        className={`mb-4 grid grid-cols-4 gap-2 font-bold ${
          darkMode ? "text-black" : "text-black"
        }`}
      >
        <QuantityButton
          percent={10}
          onClickPercent={onClickPercent}
          disabled={price == 0}
        />
        <QuantityButton
          percent={25}
          onClickPercent={onClickPercent}
          disabled={price == 0}
        />
        <QuantityButton
          percent={50}
          onClickPercent={onClickPercent}
          disabled={price == 0}
        />
        <QuantityButton
          percent={100}
          onClickPercent={onClickPercent}
          disabled={price == 0}
        />
      </div>

      {/* 증거금률 적용 체크박스 및 선택 박스 */}
      <div className="mb-4 flex justify-between">
        <div className="flex items-center w-1/2">
          <input
            id="marginCheckbox"
            type="checkbox"
            className="mr-2 transform scale-150"
            checked={isMarginApplied}
            onChange={toggleMargin}
          />
          <label htmlFor="marginCheckbox" className="text-sm font-bold">
            미수거래
          </label>
        </div>
        <select
          value={selectedMarginRate}
          onChange={handleMarginRateChange}
          className={`w-1/2 p-2 border border-gray-400 rounded-md font-bold ${
            darkMode ? "text-black" : "text-black"
          }`}
          disabled={!isMarginApplied} // 증거금률 적용 시에만 선택 가능
        >
          {/* <option value={10}>10%</option>
          <option value={25}>25%</option>
          <option value={40}>40%</option>
          <option value={50}>50%</option> */}
        </select>
      </div>

      <hr />

      {/* 주문 총액 */}
      <div className="my-6">
        <div className="font-bold text-sm">주문 총액</div>
        <div className="text-right text-xl font-bold">
          {totalOrderAmount.toLocaleString("ko-KR")}원
        </div>
      </div>

      {/* 사용자가 부담해야 하는 금액 */}
      <div className="my-6">
        <div className="font-bold text-sm">내 지불액</div>
        <div className="text-right text-xl font-bold">
          {userPayableAmount.toLocaleString("ko-KR")}원
        </div>
      </div>

      {/* 내 잔고 */}
      <div className="my-4">
        <div className="font-bold text-sm">내 잔고</div> 
        <div className="text-right text-xl font-bold">
          {activeTabOption === "buy" ? (myAccount - userPayableAmount).toLocaleString("ko-KR") : (myAccount + userPayableAmount).toLocaleString("ko-KR")}원
        </div>
      </div>
    </div>
  );
};

export default OrderStockInput;
