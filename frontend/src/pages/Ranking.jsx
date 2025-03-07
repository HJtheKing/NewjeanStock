import React, { useState } from "react";
import RankingTabs from "../components/Ranking/RankingTabs";
import TotalRanking from "../components/Ranking/TotalRanking";
import RivalRanking from "../components/Ranking/RivalRanking";
import NowRanking from "../components/Ranking/NowRanking";
import useDarkModeStore from "../store/useDarkModeStore";

const Ranking = () => {
  const { darkMode } = useDarkModeStore((state) => ({
    darkMode: state.darkMode,
  }));

  const [activeTabOption, setActiveTabOption] = useState("");
  const getActiveTabOption = (data) => {
    setActiveTabOption(data);
  };

  const selectedRanking = (tabOption) => {
    switch (tabOption) {
      case "totalRanking":
        return <TotalRanking />;
      case "rivalRanking":
        return <RivalRanking />;
      case "nowRanking":
        return <NowRanking />;
      default:
        null;
    }
  };

  return (
    <div className="mt-10 mx-24 mb-24 pt-16">
      <RankingTabs
        getActiveTabOption={getActiveTabOption}
        isDarkMode={darkMode}
      />
      {selectedRanking(activeTabOption)}
    </div>
  );
};

export default Ranking;
