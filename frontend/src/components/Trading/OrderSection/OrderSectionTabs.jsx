import React from "react";
import { useState, useEffect } from "react";

const tabsData = [
  {
    id: "order",
    label: "주문하기",
  },
  {
    id: "orderList",
    label: "체결 내역",
  },
  {
    id: "account",
    label: "보유 현황",
  },
];

const OrderSectionTabs = ({ getActiveTabOption, isDarkMode }) => {
  const [activeTab, setActiveTab] = useState(tabsData[0].id);
  useEffect(() => {
    getActiveTabOption(activeTab);
  }, [activeTab]);
  return (
    <>
      <div className="mb-2 border-b border-gray-200">
        <ul
          className="flex flex-wrap -mb-px text-sm text-center justify-evenly"
          id="default-tab"
          role="tablist"
        >
          {tabsData.map((tab) => (
            <li className="flex-auto" role="presentation" key={tab.id}>
              <button
                className={`inline-block w-full p-4 border-b-2 ${
                  activeTab === tab.id
                    ? isDarkMode
                      ? "font-bold text-white border-white" // 다크 모드일 때 활성 탭
                      : "font-bold text-slate-950 border-slate-950" // 일반 모드일 때 활성 탭
                    : isDarkMode
                      ? "font-bold text-gray-400 border-transparent hover:text-gray-300 hover:border-gray-300" // 다크 모드일 때 비활성 탭
                      : "font-bold text-gray-500 border-transparent hover:text-gray-600 hover:border-gray-300" // 일반 모드일 때 비활성 탭
                }`}
                id={`${tab.id}-tab`}
                type="button"
                role="tab"
                aria-controls={tab.id}
                aria-selected={activeTab === tab.id}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            </li>
          ))}
        </ul>
      </div>
    </>
  );
};

export default OrderSectionTabs;
