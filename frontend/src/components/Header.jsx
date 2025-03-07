import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import { MdOutlineWbSunny, MdOutlineDarkMode } from "react-icons/md"; // 다크모드 아이콘 임포트
import { CgProfile } from "react-icons/cg";
import { FiSettings } from "react-icons/fi";

import Button from "./Button.jsx";
import Modal from "./Modal.jsx";

import LogoNameImage from "./../assets/logoName.svg";
import LogoNameDarkImage from "./../assets/logoNameDark.svg"; // 다크모드 로고 추가

import useLoginUserInfoStore from "../store/useLoginUserInfoStore.js";
import useKakaoLoginCheckStore from "../store/useKakaoLoginCheckStore";
import useDarkModeStore from "../store/useDarkModeStore"; // Zustand 스토어 임포트

import SettingsModalNotification from "./SettingsModal/SettingsModalNotification.jsx";
import SettingsModalNicknameChange from "./SettingsModal/SettingsModalNicknameChange.jsx";
import SettingsModalPasswordChange from "./SettingsModal/SettingsModalPasswordChange.jsx";
import PostLogout from "./SettingsModal/PostLogout.js";

const Header = () => {
  // 1. 로그아웃 기능 관련
  const nav = useNavigate();

  const clearLoginUserInfo = useLoginUserInfoStore(
    (state) => state.clearLoginUserInfo
  );
  const setCheckKaKaoUser = useKakaoLoginCheckStore(
    (state) => state.setCheckKaKaoUser
  );
  const handleLogout = () => {
    PostLogout(nav, clearLoginUserInfo, setCheckKaKaoUser);
  };

  // 2. 설정 드롭다운 기능 관련
  const [isOpen, setIsOpen] = useState(false);
  // const toggleDropdown = () => {
  //   setIsOpen(!isOpen);
  // };

  const loginId = useLoginUserInfoStore((state) => state.loginUserInfo.loginId);
  const loginUserInfo = useLoginUserInfoStore((state) => state.loginUserInfo);

  // 3. 각 모달 열고 닫기 상태 관리
  const [isNotificationModalOpen, setIsNotificationModalOpen] = useState(false);
  const [isNicknameModalOpen, setIsNicknameModalOpen] = useState(false);
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);

  // 다크 모드 상태 관리
  const { darkMode, toggleDarkMode } = useDarkModeStore(); // Zustand 스토어에서 다크 모드 상태와 토글 함수 가져오기

  // 페이지 로드 시 localStorage에서 테마 상태 불러오기

  // console.log("지금 로그인한 loginMemberId: ", loginId);
  // console.log(loginUserInfo);

  // 모달 외부 클릭 감지 ref
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      // dropdownRef가 가리키는 영역 바깥을 클릭한 경우
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false); // 드롭다운 닫기
      }
    };
    // mousedown 이벤트로 외부 클릭 감지
    document.addEventListener("mousedown", handleClickOutside);

    // 컴포넌트 언마운트 시 이벤트 리스너 해제
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div>
      <header className="absolute inset-x-0 top-0 z-50 left-0 bg-white dark:bg-grayDark text-grayDark dark:text-white body-font border-b border-gray-200 dark:border-gray-600">
        {/* <div className="container mx-auto flex flex-wrap p-5 flex-col md:flex-row items-center md:h-28"> */}
        <div className="flex flex-wrap py-2 px-20 flex-row items-center h-18">
          <div className="flex title-font font-medium items-center text-gray-900 mb-0">
            {/* <div className="flex title-font font-medium items-center text-gray-900 mb-4 md:mb-0"> */}
            <Link to={"/"}>
              {/* 다크 모드에 따라 로고 이미지 변경 */}
              <img
                alt="logo"
                src={darkMode ? LogoNameDarkImage : LogoNameImage}
                className="w-28 inline-block"
              />
            </Link>
          </div>
          <nav className="ml-auto flex flex-wrap items-center text-base justify-center font-bold text-gray-600">
            {/* <nav className="md:ml-auto flex flex-wrap items-center text-base justify-center"> */}
            <Link to={"/trading/005930"}>
              <span className="mr-5 hover:text-gray-900">대회</span>
            </Link>
            <Link to={"/ranking"}>
              <span className="mr-5 hover:text-gray-900">랭킹</span>
            </Link>
            <div>
              <div
                className="relative inline-block text-left"
                ref={dropdownRef}
              >
                <div>
                  <button
                    type="button"
                    className=" w-full justify-center gap-x-1.5 rounded-md pr-5 mr-5text-sm font-semibold text-gray-600 "
                    id="menu-button"
                    aria-expanded="true"
                    aria-haspopup="true"
                    onClick={() => setIsOpen(!isOpen)}
                  >
                    <FiSettings className=" w-6 h-6 inline-block" />
                  </button>
                </div>
                {isOpen && (
                  <div
                    className={`font-bold absolute -left-[150%] z-10 mt-2 w-40 origin-top-right divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none ${
                      darkMode
                        ? "bg-gray-800 text-white"
                        : "bg-white text-black"
                    }`}
                    role="menu"
                    aria-orientation="vertical"
                    aria-labelledby="menu-button"
                    tabIndex="-1"
                  >
                    <div className="flex justify-center py-1" role="none">
                      <button
                        className="block w-full py-2 text-center text-sm text-gray-500 hover:text-gray-900"
                        role="menuitem"
                        tabIndex="-1"
                        id="menu-item-0"
                        onClick={() => setIsNotificationModalOpen(true)}
                      >
                        알림 설정
                      </button>
                      <Modal
                        isOpen={isNotificationModalOpen}
                        closeModal={() => setIsNotificationModalOpen(false)}
                      >
                        <SettingsModalNotification
                          closeModal={() => setIsNotificationModalOpen(false)}
                        />
                      </Modal>
                    </div>
                    <div>
                      <div className="flex justify-center py-1" role="none">
                        <button
                          className="block w-full py-2 text-sm text-center text-gray-500 hover:text-gray-900"
                          role="menuitem"
                          tabIndex="-1"
                          id="menu-item-2"
                          onClick={() => setIsNicknameModalOpen(true)}
                        >
                          닉네임 변경
                        </button>
                        <Modal
                          isOpen={isNicknameModalOpen}
                          closeModal={() => setIsNicknameModalOpen(false)}
                        >
                          <SettingsModalNicknameChange
                            closeModal={() => setIsNicknameModalOpen(false)}
                          />
                        </Modal>
                      </div>
                      <div className="flex justify-center py-1" role="none">
                        <button
                          className="block w-full py-2 text-sm text-center text-gray-500 hover:text-gray-900"
                          role="menuitem"
                          tabIndex="-1"
                          id="menu-item-3"
                          onClick={() => setIsPasswordModalOpen(true)}
                        >
                          비밀번호 변경
                        </button>
                        <Modal
                          isOpen={isPasswordModalOpen}
                          closeModal={() => setIsPasswordModalOpen(false)}
                        >
                          <SettingsModalPasswordChange
                            closeModal={() => setIsPasswordModalOpen(false)}
                          />
                        </Modal>
                      </div>
                    </div>
                    <div
                      className="p-3 flex justify-center text-black dark:text-black"
                      role="none"
                    >
                      <Button
                        text={"로그아웃"}
                        color={"yellow2"}
                        onClick={handleLogout}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>
            {/* 다크 모드 버튼 추가 */}
            <button
              onClick={toggleDarkMode}
              className="mr-5 w-6 h-6 inline-block"
              aria-label="Toggle Dark Mode"
            >
              {darkMode ? (
                <MdOutlineDarkMode size={24} className="text-gray-600" />
              ) : (
                <MdOutlineWbSunny size={24} className="text-gray-600" />
              )}
            </button>
            <Link to={`/profile/${loginId}`}>
              <CgProfile className="mr-5 w-6 h-6 inline-block" />
            </Link>
          </nav>
        </div>
      </header>
    </div>
  );
};

export default Header;
