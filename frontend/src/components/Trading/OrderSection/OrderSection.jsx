import React, { useState } from "react";
import OrderSectionTabs from "./OrderSectionTabs";
import OrderMain from "./OrderMain/OrderMain";
import OrderList from "./OrderList/OrderList";
import Account from "./Account";
import OrderContestInfo from "./OrderContestInfo";
import useDarkModeStore from "../../../store/useDarkModeStore"; // 다크 모드 스토어 가져오기

const OrderSection = ({ stockId }) => {
  const [activeTabOption, setActiveTabOption] = useState("");
  const getActiveTabOption = (data) => {
    setActiveTabOption(data);
  };

  // 다크 모드 상태 가져오기
  const isDarkMode = useDarkModeStore((state) => state.darkMode);

  const selectedSection = (tabOption) => {
    switch (tabOption) {
      case "order":
        return <OrderMain />;
      case "orderList":
        return <OrderList />;
      case "account":
        return <Account />;
      default:
        null;
    }
  };

  return (
    <div className="m-2">
      <OrderContestInfo />
      {/* 다크 모드 상태를 OrderSectionTabs에 전달 */}
      <OrderSectionTabs
        getActiveTabOption={getActiveTabOption}
        isDarkMode={isDarkMode}
      />
      {selectedSection(activeTabOption)}
    </div>
  );
};

export default OrderSection;
