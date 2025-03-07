import React, { useState } from "react";
import { Link } from "react-router-dom";
import 삼성전자 from "./../../assets/companylogo/005930.png";
import 삼성전기 from "./../../assets/companylogo/009150.png";
import SK하이닉스 from "./../../assets/companylogo/000660.png";
import 셀리드 from "./../../assets/companylogo/299660.png";
import 한미반도체 from "./../../assets/companylogo/042700.png";
import NAVER from "./../../assets/companylogo/035420.png";
import 카카오 from "./../../assets/companylogo/035720.png";
import HLB from "./../../assets/companylogo/028300.png";
import 랩지노믹스 from "./../../assets/companylogo/084650.png";
import 실리콘투 from "./../../assets/companylogo/257720.png";

import useDarkModeStore from "../../store/useDarkModeStore"; // 다크 모드 상태 가져오기

const stockList = [
  {
    stockId: "005930",
    companyName: "삼성전자",
    logo: 삼성전자,
  },
  {
    stockId: "009150",
    companyName: "삼성전기",
    logo: 삼성전기,
  },
  {
    stockId: "000660",
    companyName: "SK하이닉스",
    logo: SK하이닉스,
  },
  {
    stockId: "299660",
    companyName: "셀리드",
    logo: 셀리드,
  },
  {
    stockId: "042700",
    companyName: "한미반도체",
    logo: 한미반도체,
  },
  {
    stockId: "035420",
    companyName: "NAVER",
    logo: NAVER,
  },
  {
    stockId: "035720",
    companyName: "카카오",
    logo: 카카오,
  },
  {
    stockId: "028300",
    companyName: "HLB",
    logo: HLB,
  },
  {
    stockId: "084650",
    companyName: "랩지노믹스",
    logo: 랩지노믹스,
  },
  {
    stockId: "257720",
    companyName: "실리콘투",
    logo: 실리콘투,
  },
];

const StockList = () => {
  // 다크 모드 상태 가져오기
  const { darkMode } = useDarkModeStore((state) => ({
    darkMode: state.darkMode,
  }));

  // 선택된 주식 상태
  const [selectedStockId, setSelectedStockId] = useState(null);

  return (
    <div className={`m-3 font-bold ${darkMode ? "text-black" : "text-black"}`}>
      <div
        className={`font-bold border-b-2 ${darkMode ? "border-gray-400 text-gray-200" : "border-black text-black"} pb-3 my-6 text-center`}
      >
        전체 종목
      </div>
      {/* 반복할 부분 */}
      {stockList.map((holding, index) => (
        <Link to={`/trading/${holding.stockId}`} key={index}>
          <div
            className="flex justify-between border-b p-3"
            onClick={() => setSelectedStockId(holding.stockId)} // 클릭 시 선택된 주식 설정
          >
            <div className="flex gap-2">
              <img src={holding.logo} alt="" className="w-6 h-6 rounded-full" />
              <div
                className={`font-bold ${selectedStockId === holding.stockId ? "text-white" : darkMode ? "text-gray-400" : "text-black"}`} // 선택된 주식은 흰색, 다크 모드에서는 회색, 일반 모드에서는 검은색
              >
                {holding.companyName}
              </div>
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
};

export default StockList;
