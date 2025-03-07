import React from "react";
import Button from "../Button";
import { useNavigate } from "react-router-dom";

const HomeCenterNoCompetitionBeforeLogin = () => {
  const nav = useNavigate();
  const onClick = () => {
    nav("/login");
    // 로그인으로 nav
  };

  return (
    <div className="py-16 w-1/2 mx-auto">
      <div className="my-5  font-bold">
        <div className="text-xl pb-1">당신을 위한 모의주식 투자</div>
        <div className="text-gray-500">뉴진스톡에서 시작하세요 🐣</div>
      </div>
      <div className="button-text px-4 py-2 rounded">
        <Button text={"로그인"} onClick={onClick} />
      </div>
    </div>
  );
};

export default HomeCenterNoCompetitionBeforeLogin;
