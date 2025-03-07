import React from "react";
import { useEffect } from "react";
import useDarkModeStore from "../store/useDarkModeStore"; // 다크 모드 스토어 import

import HomeCompetitionInfo from "../components/Home/HomeCompetitionInfo";
import HomeCenterIngCompetitionAfterLoginIsCompParticipant from "../components/Home/HomeCenterIngCompetitionAfterLoginIsCompParticipant";
import HomeCenterIngCompetitionAfterLoginNotCompParticipant from "../components/Home/HomeCenterIngCompetitionAfterLoginNotCompParticipant";
import HomeCenterIngCompetitionBeforeLogin from "../components/Home/HomeCenterIngCompetitionBeforeLogin";
import HomeCenterNoCompetitionAfterLogin from "../components/Home/HomeCenterNoCompetitionAfterLogin";
import HomeCenterNoCompetitionBeforeLogin from "../components/Home/HomeCenterNoCompetitionBeforeLogin";

import HomeBottom from "../components/Home/HomeBottom";
import Input from "../components/Input";
import Modal from "../components/Modal";

import useCompetitionInfoStore from "../store/useCompetitionInfoStore";
import useLoginUserInfoStore from "../store/useLoginUserInfoStore";

// Date converted to YYYY. MM. DD format
const handleDate = (strDate) => {
  const date = new Date(strDate);
  return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}`;
};

const Home = () => {
  // 다크모드 상태 관리
  const { darkMode } = useDarkModeStore(); // 다크 모드 상태 가져오기

  // 페이지 로드 시 localStorage에서 테마 상태 불러오기
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [darkMode]);

  // const check = window.localStorage.getItem("checkKaKaoUser");
  // const checkObj = JSON.parse(check);
  // console.log(checkObj);
  // const checkKaKaoUser = checkObj.checkKaKaoUser;
  // console.log("checkKaKaoUser", checkKaKaoUser);

  // if (checkKaKaoUser === true) {
  //   window.location.reload();
  // }
  const ingCompetition = useCompetitionInfoStore(
    (state) => state.competitionInfo.ingCompetition
  );
  // console.log("ingCompetition: ", ingCompetition);

  const isLogined = useLoginUserInfoStore(
    (state) => state.loginUserInfo.isLogined
  );
  // console.log("isLogined: ", isLogined);

  const isCompParticipant = useLoginUserInfoStore(
    (state) => state.loginUserInfo.isCompParticipant
  );
  // console.log("isCompParticipant: ", isCompParticipant);

  return (
    <div className={`flex flex-col h-screen pt-16 ${darkMode ? "dark" : ""}`}>
      <div className="flex-none">
        <HomeCompetitionInfo handleDate={handleDate} />
      </div>
      <div className="flex-none">
        {ingCompetition ? (
          isLogined ? (
            isCompParticipant ? (
              <HomeCenterIngCompetitionAfterLoginIsCompParticipant />
            ) : (
              <HomeCenterIngCompetitionAfterLoginNotCompParticipant />
            )
          ) : (
            <HomeCenterIngCompetitionBeforeLogin />
          )
        ) : isLogined ? (
          <HomeCenterNoCompetitionAfterLogin />
        ) : (
          <HomeCenterNoCompetitionBeforeLogin />
        )}
      </div>
      <div className="flex-1">
        <HomeBottom />
      </div>
    </div>
  );
};

export default Home;
