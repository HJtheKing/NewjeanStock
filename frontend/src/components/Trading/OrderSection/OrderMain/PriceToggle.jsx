import { useState } from "react";
import useDarkModeStore from "./../../../../store/useDarkModeStore"; // Import the dark mode store

const PriceToggle = ({ setIsMarketPrice, isMarketPrice }) => {
  const [activeOption, setActiveOption] = useState("limitPrice");

  // Use dark mode state
  const { darkMode } = useDarkModeStore((state) => ({
    darkMode: state.darkMode,
  }));

  const handleSwitchClick = (option) => {
    setActiveOption(option);
    option === "marketPrice" ? setIsMarketPrice(true) : setIsMarketPrice(false);
  };

  return (
    <div
      className={`flex bg-gray-300 p-[3px] rounded-full w-32 ml-auto font-bold my-0 text-sm ${darkMode ? "bg-gray-300" : "bg-gray-300"}`}
    >
      <div
        className={`flex-auto rounded-full text-center ${activeOption === "limitPrice" ? "bg-yellow2" : "text-white"} ${darkMode ? "text-black" : "text-black"}`}
        onClick={() => handleSwitchClick("limitPrice")}
        role="button"
      >
        <div>지정가</div>
      </div>
      <div
        className={`flex-auto rounded-full text-center ${activeOption === "marketPrice" ? "bg-yellow2" : "text-white"} ${darkMode ? "text-black" : "text-black"}`}
        onClick={() => handleSwitchClick("marketPrice")}
        role="button"
      >
        <div>시장가</div>
      </div>
    </div>
  );
};

export default PriceToggle;
