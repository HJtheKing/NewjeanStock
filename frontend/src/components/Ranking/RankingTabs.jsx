import React, { useState, useEffect } from "react";

const tabsData = [
  {
    id: "totalRanking",
    label: "전체 순위",
  },
  {
    id: "rivalRanking",
    label: "친구 순위",
  },
];

const RankingTabs = ({ getActiveTabOption, isDarkMode }) => {
  const [activeTab, setActiveTab] = useState(tabsData[0].id);

  useEffect(() => {
    getActiveTabOption(activeTab);
  }, [activeTab]);

  return (
    <>
      <div className="mb-4 border-b border-gray-200">
        <ul
          className="flex flex-wrap -mb-px text-sm text-center justify-evenly"
          id="default-tab"
          role="tablist"
        >
          {tabsData.map((tab) => (
            <li className="flex-auto" role="presentation" key={tab.id}>
              <button
                className={`inline-block w-full p-4 border-b-2 rounded-t-lg ${
                  activeTab === tab.id
                    ? `font-bold ${isDarkMode ? "text-white border-white" : "text-black border-slate-950"}`
                    : `font-bold ${isDarkMode ? "text-gray-400 border-slate-400" : "border-slate-950"} border-transparent hover:${isDarkMode ? "text-gray-400" : "text-gray-700"} hover:border-gray-300`
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

export default RankingTabs;
