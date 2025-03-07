import React, { useEffect } from "react";
import financialImg from "./../../assets/financial_illustration.png";

const HomeBottom = () => {
  useEffect(() => {
    if (localStorage.getItem("theme") === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, []);

  return (
    <div className="bg-gray-100 dark:bg-gray-800 h-full pt-10">
      <div className="flex gap-20 justify-center">
        <div className="min-w-40 max-w-60">
          <img src={financialImg} alt="financial image" />
        </div>
        <div className="content-center max-w-xl">
          <h2 className="text-2xl font-bold mb-5 text-gray-900 dark:text-white">
            간편하고 재미있는 모의 주식, 뉴진스톡
          </h2>
          <p className="leading-8 font-bold text-gray-500 dark:text-gray-200">
            주식 투자, 이제는 재미있게!
            <br />
            친구들과 함께 경쟁하고, 성과를 비교하며 투자 전략을 발전시켜 보세요!
          </p>
        </div>
      </div>
    </div>
  );
};

export default HomeBottom;
